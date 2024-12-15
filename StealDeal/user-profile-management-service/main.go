package main

import (
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"os"
	"userprofileservice/controllers"
	"userprofileservice/handlers"

	"github.com/gorilla/mux"
	_ "github.com/lib/pq"
)

// InitDB initializes the database and creates tables if they don't exist
func InitDB(db *sql.DB) {
	createUsersTable := `
    CREATE TABLE IF NOT EXISTS users (
        id SERIAL PRIMARY KEY,
        username VARCHAR(50) UNIQUE NOT NULL,
        email VARCHAR(100) UNIQUE NOT NULL,
        hashed_password VARCHAR(255) NOT NULL,
        personal_details TEXT,
        private BOOLEAN DEFAULT false,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );`

	_, err := db.Exec(createUsersTable)
	if err != nil {
		log.Fatalf("Error creating users table: %v", err)
	}
	// Create an index on the username column of the users table
	createIndex := `CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);`
	_, err = db.Exec(createIndex)
	if err != nil {
		log.Fatalf("Error creating index on users table: %v", err)
	}

}

// CORS Middleware to allow cross-origin requests
func corsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// Set headers to allow everything
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
		w.Header().Set("Access-Control-Allow-Headers", "Accept, Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization")

		// Check if the request method is OPTIONS for preflight request
		if r.Method == "OPTIONS" {
			// Respond with 200 OK without passing the request down the handler chain
			w.WriteHeader(http.StatusOK)
			return
		}

		// Pass the request down the handler chain
		next.ServeHTTP(w, r)
	})
}

func main() {
	// Initialize the database connection
	db, err := sql.Open("postgres", fmt.Sprintf("host=%s port=%s user=%s "+
		"password=%s dbname=%s",
		os.Getenv("DB_HOST"), os.Getenv("DB_PORT"),
		os.Getenv("DB_USER"), os.Getenv("DB_PASSWORD"),
		os.Getenv("DB_NAME")))
	if err != nil {
		log.Fatal("Error connecting to the database: ", err)
	}
	defer db.Close()
	InitDB(db)
	// Initialize UserController with the database connection
	userController := controllers.NewUserController(db)

	// Initialize UserHandler with UserController
	userHandler := handlers.NewUserHandler(userController)

	router := mux.NewRouter()

	// Apply CORS middleware to all routes
	router.Use(corsMiddleware)

	// Define routes
	router.HandleFunc("/users", userHandler.CreateUserHandler).Methods("POST")
	router.HandleFunc("/users/{id}", userHandler.GetUserHandler).Methods("GET")
	router.HandleFunc("/users/{id}", userHandler.UpdateUserHandler).Methods("PUT")
	router.HandleFunc("/users/{id}", userHandler.DeleteUserHandler).Methods("DELETE")
	router.HandleFunc("/users", userHandler.GetAllUsersHandler).Methods("GET")
	// Start the HTTP server
	log.Println("Starting server on :8088")
	log.Fatal(http.ListenAndServe(":8088", router))
}
