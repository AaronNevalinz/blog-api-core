# Blog API

This project is a RESTful API that allows users to manage blog posts, including associating topics and uploading images. It supports creating posts with multiple topics and file uploads (e.g., post images). The API is designed using **Spring Boot** and follows best practices for file handling and data persistence.

## Features

- **Post Management:** Create and retrieve blog posts.
- **Topic Association:** Posts can have multiple associated topics.
- **File Uploads:** Upload images for posts.
- **Validation:** The API validates input using annotations and handles exceptions appropriately.
- **Database:** Uses a relational database (e.g., MySQL) for storing posts and topics.

## Technologies Used

- **Spring Boot**: For building the RESTful API.
- **Spring Data JPA**: For database operations.
- **Amazon S3**: For storing uploaded images.
- **Validation**: Java Bean Validation annotations for validating incoming data.
- **H2 or MySQL**: Used for development and production databases.
- **Jackson**: For JSON binding.

## Requirements

- **Java 11 or higher** 
- **Spring Boot 2.x**
- **Maven 3.x or Gradle**

## Setup Instructions

### Clone the repository:

```bash
git clone https://github.com/yourusername/blog-api.git
cd blog-api
```

### Setup Environment Variables

Make sure you have the following environment variables set up for local development:

- `AWS_ACCESS_KEY`: Your AWS access key.
- `AWS_SECRET_KEY`: Your AWS secret key.
- `AWS_S3_BUCKET_NAME`: Your S3 bucket name.

### Build the project

If you're using Maven:

```bash
mvn clean install
```

If you're using Gradle:

```bash
gradle build
```

### Run the application

To run the application, use:

```bash
mvn spring-boot:run
```

or with Gradle:

```bash
gradle bootRun
```

The application will run on `http://localhost:8080`.

## API Endpoints

### **Create a Post**

- **URL**: `/add-post`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`

#### Request body (Form-data):

| Key   | Type  | Description                                      |
|-------|-------|--------------------------------------------------|
| `post` | **Text** | JSON object containing the post details (title, content, topics). |
| `image` | **File** | The image to be uploaded.                       |

#### Request example:

```json
{
  "post": {
    "title": "My First Post",
    "content": "This is the content of my first post.",
    "topics": [
      {"name": "Java"},
      {"name": "Spring Boot"}
    ]
  },
  "image": (file)
}
```

#### Response:

```json
{
  "status": "true",
  "result": {
    "id": 1,
    "title": "My First Post",
    "content": "This is the content of my first post.",
    "topics": [
      {"name": "Java"},
      {"name": "Spring Boot"}
    ],
    "imgUrl": "https://s3.amazonaws.com/yourbucket/filename.jpg"
  }
}
```

### **Error Handling**

- All validation errors will return a `400 Bad Request` with a list of validation messages.
- Any internal server errors will return a `500 Internal Server Error` with an error message.

### Example of Error Response (validation errors):

```json
{
  "status": false,
  "errors": {
    "title": "Title cannot be empty",
    "content": "Content cannot be empty"
  }
}
```

---

## File Upload Handling

This application uses **Amazon S3** to store the uploaded images. The images are uploaded via the API, and the URL is returned and stored in the `Post` entity.

Ensure that your AWS credentials are set up correctly in your environment variables:

- `AWS_ACCESS_KEY`
- `AWS_SECRET_KEY`
- `AWS_S3_BUCKET_NAME`

---

## Database Structure

- **Post Table**: Stores blog posts, including their title, content, and image URL.
- **Topic Table**: Stores topics associated with posts.
  - Each topic has a name.
- **Many-to-Many Relationship**: Posts can have multiple topics, and topics can be associated with multiple posts.

---

## Error Handling

- **MethodArgumentNotValidException**: Returns field-specific validation errors.
- **Other exceptions**: Handled by a global exception handler to return a clear and structured error response.

---

## Future Improvements

- **Authentication**: Implement user authentication and authorization using JWT or OAuth2.
- **Pagination**: Add pagination for fetching posts and topics.
- **Search**: Implement search functionality for posts by title or content.
- **Rate Limiting**: Prevent abuse by adding rate-limiting functionality.

---

## Contribution

Feel free to fork and submit pull requests. Please follow the standard Git flow for contributions:

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Push to your forked repository.
5. Create a pull request.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
