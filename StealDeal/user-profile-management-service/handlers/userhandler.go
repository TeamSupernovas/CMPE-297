package handlers

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strconv"
	"userprofileservice/controllers"

	"github.com/gorilla/mux"
)

type UserHandler struct {
	Controller *controllers.UserController
}

func NewUserHandler(controller *controllers.UserController) *UserHandler {
	return &UserHandler{Controller: controller}
}

// CreateUserHandler handles the creation of a new user
func (uh *UserHandler) CreateUserHandler(w http.ResponseWriter, r *http.Request) {
	var user struct {
		Username        string
		Email           string
		Password        string // In a real application, ensure this is securely handled
		Private         bool
		PersonalDetails string
	}

	err := json.NewDecoder(r.Body).Decode(&user)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	// Here, you would typically hash the password. For simplicity, we're using it as-is.
	userID, err := uh.Controller.CreateUser(user.Username, user.Email, user.Password, user.Private, sql.NullString{String: user.PersonalDetails, Valid: user.PersonalDetails != ""})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(map[string]int{"id": userID})
}

// GetUserHandler handles retrieving a user by their ID
func (uh *UserHandler) GetUserHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	userID, err := strconv.Atoi(vars["id"])
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	user, err := uh.Controller.GetUser(userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(user)
}

// ... [previous code in handlers/userhandler.go]

// UpdateUserHandler handles the update of a user's details
func (uh *UserHandler) UpdateUserHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	userID, err := strconv.Atoi(vars["id"])
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	var user struct {
		Username        string
		Email           string
		Password        string // Ensure secure handling in a real application
		Private         bool
		PersonalDetails string
	}

	err = json.NewDecoder(r.Body).Decode(&user)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	// Here, you would typically hash the password. For simplicity, we're using it as-is.
	err = uh.Controller.UpdateUser(userID, user.Username, user.Email, user.Password, user.Private, sql.NullString{String: user.PersonalDetails, Valid: user.PersonalDetails != ""})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]string{"status": "success"})
}

// DeleteUserHandler handles the deletion of a user
func (uh *UserHandler) DeleteUserHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	userID, err := strconv.Atoi(vars["id"])
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	err = uh.Controller.DeleteUser(userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// GetAllUsersHandler handles the request to get all users
func (uh *UserHandler) GetAllUsersHandler(w http.ResponseWriter, r *http.Request) {
	users, err := uh.Controller.GetAllUsers()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(users)
}
