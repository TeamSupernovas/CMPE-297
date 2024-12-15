package controllers

import (
	"database/sql"
	"userprofileservice/models"
)

type UserController struct {
	DB *sql.DB
}

func NewUserController(db *sql.DB) *UserController {
	return &UserController{DB: db}
}

// CreateUser handles the logic for creating a new user
func (uc *UserController) CreateUser(username, email, hashedPassword string, private bool, personalDetails sql.NullString) (int, error) {
	userID, err := models.CreateUser(uc.DB, username, email, hashedPassword, private, personalDetails)
	if err != nil {
		return 0, err
	}
	return userID, nil
}

// GetUser handles the logic for retrieving a user by ID
func (uc *UserController) GetUser(id int) (*models.User, error) {
	user, err := models.GetUserByID(uc.DB, id)
	if err != nil {
		return nil, err
	}
	return user, nil
}

// UpdateUser handles the logic for updating a user's details
func (uc *UserController) UpdateUser(id int, username, email, hashedPassword string, private bool, personalDetails sql.NullString) error {
	err := models.UpdateUser(uc.DB, id, username, email, hashedPassword, private, personalDetails)
	return err
}

// DeleteUser handles the logic for deleting a user
func (uc *UserController) DeleteUser(id int) error {
	err := models.DeleteUser(uc.DB, id)
	return err
}

// GetAllUsers retrieves all users from the database
func (uc *UserController) GetAllUsers() ([]*models.User, error) {
	return models.GetAllUsers(uc.DB)
}
