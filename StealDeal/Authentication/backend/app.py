from flask import Flask, request, jsonify, send_file
import firebase_admin
from firebase_admin import credentials, firestore
import requests
import base64
import pyotp
from PIL import Image
from io import BytesIO
import uuid
from flask import Flask, request, jsonify, session
from flask_session import Session
from flask_cors import CORS  # Import CORS
import firebase_admin
from firebase_admin import credentials, firestore
import pyotp
import qrcode
from firebase_admin import credentials, auth
import requests
import logging
import jwt
import os

# Inside the registration function
user_uid = str(uuid.uuid4())

FIREBASE_WEB_API_KEY = os.environ.get('FIREBASE_WEB_API_KEY')
# Initialize Firebase Admin SDK
cred = credentials.Certificate(os.environ.get('CREDENTIALS_FILE'))
firebase_admin.initialize_app(cred)

# Firestore database
db = firestore.client()

app = Flask(__name__)
app.config["SESSION_PERMANENT"] = False
app.config["SESSION_TYPE"] = "filesystem"
Session(app)

# Enable CORS
CORS(app)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

"""####################### REGISTRATION FLOW ############################"""

@app.route('/auth/users', methods=['POST'])
def register_user():
    data = request.json
    # Send POST request to external service to get user ID
    #logger.info(data)
    logger.info("Registering User")
    response = requests.post(os.environ.get('USER_SERVICE_URL'), json=data)
    logger.info(response)
    if response.status_code != 201:
        return jsonify({"error": "Failed to get user ID"}), response.status_code
    #logger.info(response.json())
    user_id = response.json().get("id")

    # Generate a 2FA secret
    secret = pyotp.random_base32()

    # Prepare user data for Firestore
    user_data = {
        "Username": data.get("Username"),
        "Email": data.get("Email"),
        "HashedPassword": data.get("HashedPassword"),
        "PersonalDetails": data.get("PersonalDetails"),
        "Private": data.get("Private"),
        "UserID": user_id,
        "TwoFactorSecret": secret
    }

    # Store in Firestore using email as the document ID
    db.collection('users').document(data.get("Email")).set(user_data)
    # On successful registration, save user details in session
    session['username'] = data.get('Username')
    session['email'] = data.get('Email')
    session['user_id'] = user_id  # Assuming user_id is obtained as part of registration
    logger.info("Registeration Successful")
    return jsonify({"email": data.get("Email"), "userID": user_id, "secret": secret,"username":data.get('Username')}), 201

@app.route('/auth/qr_code', methods=['GET'])
def qr_code():
    user_email = request.args.get('email')
    if not user_email:
        return jsonify({"error": "Email is required"}), 400

    # Retrieve user's 2FA secret from Firestore
    user_ref = db.collection('users').document(user_email)
    user_doc = user_ref.get()
    if not user_doc.exists:
        return jsonify({"error": "User not found"}), 404

    secret = user_doc.to_dict().get('TwoFactorSecret')

    # Generate QR code
    uri = pyotp.totp.TOTP(secret).provisioning_uri(name=user_email, issuer_name='expressNest')
    qr = qrcode.QRCode(
    version=1,
    error_correction=qrcode.constants.ERROR_CORRECT_L,
    box_size=10,
    border=4,
    )
    qr.add_data(uri)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    # Save QR Code to a BytesIO buffer

    buffered = BytesIO()
    img.save(buffered, format="PNG")
    img_str = base64.b64encode(buffered.getvalue()).decode()
    return jsonify({"qr_code": f"data:image/png;base64,{img_str}"})
    return send_file(buf, mimetype='image/jpeg')

@app.route('/auth/validate_2fa', methods=['POST'])
def validate_2fa():
    data = request.json
    user_email = data.get("email")
    token = data.get("token")
    logger.info("Two factor authentication started")

    if not user_email or not token:
        return jsonify({"error": "Email and token are required"}), 400

    # Retrieve user's 2FA secret from Firestore
    user_ref = db.collection('users').document(user_email)
    user_doc = user_ref.get()
    if not user_doc.exists:
        return jsonify({"error": "User not found"}), 404

    secret = user_doc.to_dict().get('TwoFactorSecret')
    totp = pyotp.TOTP(secret)
    user_data = user_doc.to_dict()
    role = "USER"
    if("ROLE" in user_data):
        role = "ADMIN"
    # Verify the 2FA token
    encoded_jwt = jwt.encode({"ROLE": role,"userID":user_data.get('UserID')}, os.environ.get('JWT_SECRET'), algorithm="HS256")
    logger.info("Two factor authentication completed")
    if totp.verify(token):
        return jsonify({"status":"2FA verified","ROLE":role,"email": user_email, "username": user_data.get('Username'), "userID":user_data.get('UserID'), "jwt" :encoded_jwt}), 200
    else:
        return jsonify({"error": "Invalid 2FA token"}), 400

@app.route('/auth/validate_2fa_v2', methods=['POST'])
def validate_2fa_v2():
    data = request.json
    user_email = data.get("email")
    token = data.get("token")
    logger.info("Two factor authentication started")
    if not user_email or not token:
        return jsonify({"error": "Email and token are required"}), 400

    # Query for a user with the given username
    users_ref = db.collection('users')
    query = users_ref.where('Username', '==', user_email)
    results = query.stream()

    # Fetch the first user that matches the query
    user_data = None
    for doc in results:
        user_data = doc.to_dict()
        break
    logging.info(user_data)
    
    if not user_data:
        return jsonify({"error": "User not found"}), 404

    secret = user_data.get('TwoFactorSecret')
    totp = pyotp.TOTP(secret)
    role = "USER"
    if("ROLE" in user_data):
        role = "ADMIN"
    # Verify the 2FA token
    encoded_jwt = jwt.encode({"ROLE": role,"userID":user_data.get('UserID')}, os.environ.get('JWT_SECRET'), algorithm="HS256")
    logger.info("Two factor authentication ended")
    if totp.verify(token):
        logging.info("OKAY")
        return jsonify({"ROLE": role, "status":"2FA verified", "email": user_email, "username": user_data.get('Username'), "userID":user_data.get('UserID'), "jwt": encoded_jwt}), 200
    else:
        
        return jsonify({"error": "Invalid 2FA token"}), 400

"""####################### LOGIN FLOW ############################"""

@app.route('/auth/login', methods=['POST'])
def login():
    data = request.json
    login_type = data.get("type")

    if login_type == "email":
        logger.info("Email authentication")
        return login_with_email(data)
    elif login_type == "google" or login_type == "github":
        logger.info("Google authentication in progress")
        return login_with_oauth(data, login_type)
    else:
        logger.info("Invalid login type")
        return jsonify({"error": "Invalid login type"}), 400



def login_with_email(data):
    logger = logging.getLogger()
    logger.info("Username")
    logger.info(data)
    username = data.get("email")
    password = data.get("password")  # Assume hashed password

    # Query for a user with the given username
    users_ref = db.collection('users')
    query = users_ref.where('Username', '==', username)
    results = query.stream()

    # Fetch the first user that matches the query
    user_data = None
    for doc in results:
        user_data = doc.to_dict()
        break

    # Check if user was found and verify password
    if user_data and user_data['HashedPassword'] == password:
        # Set up session
        email = doc.id  # The document ID is the email
        session['email'] = email
        session['username'] = user_data.get('Username')
        session['user_id'] = user_data.get('UserID')
        return jsonify({"status":"2FA required", "email": email, "username": user_data.get('Username'), "userID": user_data.get('UserID')}), 200
    elif user_data:
        return jsonify({"error": "Invalid credentials"}), 401
    else:
        return jsonify({"error": "User not found"}), 404



def login_with_oauth(data, provider):
    id_token = data.get("idToken")

    # Verify the Firebase ID token
    logger.info("ID Token: ")
    logger.info(id_token)
    try:
        decoded_token = auth.verify_id_token(id_token)
        firebase_user_id = decoded_token['uid']
        
        if provider == "google":
            logger.info(decoded_token)
            email = decoded_token['uid']
            logger.info(email)
        elif provider == "github":
            # Construct a pseudo-email with Firebase UID for GitHub users
            email = f"github_{firebase_user_id}@example.com"
            logger.info(email)
        else:
            return jsonify({"error": "Unsupported provider"}), 400

        # Check if this Firebase UID exists in our Firestore database
        user_ref = db.collection('users').document(email)
        user_doc = user_ref.get()
        if user_doc.exists:
            user_data = user_doc.to_dict()
            # Set up session
            session['email'] = email
            session['username'] = user_data.get('Username')
            session['user_id'] = user_data.get('UserID')
            return jsonify({"status":"2FA required","email": email, "username": user_data.get('Username'), "userID": user_data.get('UserID')}), 200
        else:
            # User not registered, return 200 status with indication
            return jsonify({"status": "User not registered", "email": email}), 200
    except Exception as e:
        logging.info(str(e))
        return jsonify({"error": str(e)}), 401


"""####################### PASSWORD RECOVERY FLOW ############################"""
@app.route('/auth/request_password_recovery', methods=['POST'])
def request_password_recovery():
    data = request.json
    user_email = data.get("email")
    token = data.get("token")

    # Verify that the email is provided
    if not user_email or not token:
        return jsonify({"error": "Email and token are required"}), 400

    # Query for a user with the given username
    users_ref = db.collection('users')
    query = users_ref.where('Username', '==', user_email)
    results = query.stream()

    # Fetch the first user that matches the query
    user_data = None
    for doc in results:
        user_data = doc.to_dict()
        break

    
    if not user_data:
        return jsonify({"error": "User not found"}), 404

    secret = user_data.get('TwoFactorSecret')

    # Verify the 2FA token
    totp = pyotp.TOTP(secret)
    if not totp.verify(token):
        return jsonify({"error": "Invalid 2FA token"}), 401

    # If verification is successful, proceed with password recovery
    # (Further steps will be implemented in subsequent chunks)

    return jsonify({"success": "2FA verification successful, proceed to reset password"}), 200

@app.route('/auth/reset_password', methods=['PUT'])
def reset_password():
    data = request.json
    user_email = data.get("email")
    new_password = data.get("newPassword")  # Ensure this is hashed if necessary

    # Verify that email and new password are provided
    if not user_email or not new_password:
        return jsonify({"error": "Email and new password are required"}), 400

    users_ref = db.collection('users')
    query = users_ref.where('Username', '==', user_email)
    results = query.stream()

    # Fetch the first user that matches the query
    user_doc = None
    for doc in results:
        user_doc = doc
        break

    if not user_doc:
        return jsonify({"error": "User not found"}), 404

    # Update the user's hashed password
    user_doc.reference.update({"HashedPassword": new_password})

    return jsonify({"success": "Password updated successfully"}), 200

if __name__ == '__main__':
    app.run(debug=True, port=8098)
