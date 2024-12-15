package models

import (
	"database/sql"
	"time"
)

// User struct represents a user in the database
type User struct {
	ID              int
	Username        string
	Email           string
	HashedPassword  string
	PersonalDetails sql.NullString
	Private         bool
	CreatedAt       time.Time
	UpdatedAt       time.Time
}

// CreateUser inserts a new user into the database
func CreateUser(db *sql.DB, username, email, hashedPassword string, private bool, personalDetails sql.NullString) (int, error) {
	var userID int
	query := `INSERT INTO users (username, email, hashed_password, private, personal_details)
              VALUES ($1, $2, $3, $4, $5) RETURNING id`
	err := db.QueryRow(query, username, email, hashedPassword, private, personalDetails).Scan(&userID)
	if err != nil {
		return 0, err
	}
	return userID, nil
}

// GetUserByID retrieves a user by their ID from the database
func GetUserByID(db *sql.DB, id int) (*User, error) {
	user := &User{}
	query := `SELECT id, username, email, personal_details, private, created_at, updated_at 
              FROM users WHERE id = $1`
	err := db.QueryRow(query, id).Scan(&user.ID, &user.Username, &user.Email, &user.PersonalDetails, &user.Private, &user.CreatedAt, &user.UpdatedAt)
	if err != nil {
		return nil, err
	}
	return user, nil
}

// ... [existing code] ...

// GetAllUsers retrieves all users from the database
func GetAllUsers(db *sql.DB) ([]*User, error) {
	var users []*User

	rows, err := db.Query("SELECT id, username, email, personal_details, private, created_at, updated_at FROM users")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	for rows.Next() {
		var user User
		err := rows.Scan(&user.ID, &user.Username, &user.Email, &user.PersonalDetails, &user.Private, &user.CreatedAt, &user.UpdatedAt)
		if err != nil {
			return nil, err
		}
		users = append(users, &user)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return users, nil
}

// UpdateUser updates a user's details in the database
func UpdateUser(db *sql.DB, id int, username, email, hashedPassword string, private bool, personalDetails sql.NullString) error {
	query := `UPDATE users SET username = $1, email = $2, hashed_password = $3, private = $4, personal_details = $5, updated_at = NOW() WHERE id = $6`
	_, err := db.Exec(query, username, email, hashedPassword, private, personalDetails, id)
	return err
}

// DeleteUser removes a user from the database by ID
func DeleteUser(db *sql.DB, id int) error {
	_, err := db.Exec("DELETE FROM users WHERE id = $1", id)
	return err
}
