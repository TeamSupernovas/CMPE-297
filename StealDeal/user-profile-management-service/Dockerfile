# Dockerfile for Go application
FROM golang:1.17 as builder

WORKDIR /app

# Copy go mod and sum files
COPY go.mod go.sum ./
# Download dependencies
RUN go mod download

# Copy the source code
COPY . .

# Build the Go app
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o userprofileservice .

# Use a smaller image to run the application
FROM alpine:latest  
RUN apk --no-cache add ca-certificates
WORKDIR /root/
COPY --from=builder /app/userprofileservice .
EXPOSE 8088
CMD ["./userprofileservice"]
