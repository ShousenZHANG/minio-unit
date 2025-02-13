# 🌟 **Minio Test Project** 🌟  

*A robust demonstration of integrating [MinIO](https://min.io) with Spring Boot for efficient object storage management!*  

---

## **🎯 Key Features**  

- 🚀 **File Upload & Download**: Effortlessly manage files using RESTful APIs.  
- 📦 **Bucket Management**: Create, delete, and list buckets seamlessly.  
- 🎪 **Configurable Minio Client**: Simplified setup via `application.yml`.  
- 🛡️ **Error Handling**: Robust centralized and custom exception mechanisms.  
- 🔗 **Extensibility**: Ready to integrate with larger systems!  

---

## **🫠 Tech Stack**  

| Technology      | Version |
| --------------- | ------- |
| **Java**        | 23+     |
| **Spring Boot** | 3.4.1   |
| **Minio SDK**   | 8.5.15  |
| **Maven**       | 3.x+    |

---

## **📎 Project Structure**  

```
Minio_test
├── src
│   ├── main
│   │   ├── java/com/minio/minio_test
│   │   │   ├── config         # Minio client configuration
│   │   │   ├── controller     # REST API endpoints
│   │   │   ├── exception      # Custom exception handling
│   │   │   ├── handler        # Global exception handler
│   │   │   ├── service        # Service layer for business logic
│   │   │   ├── vo             # Data transfer objects (VOs)
│   │   │   └── response       # API response models
│   │   └── resources
│   │       └── application.yml  # Minio configuration file
├── .mvn                        # Maven wrapper
├── pom.xml                     # Maven dependencies and build configuration
└── README.md                   # Project documentation
```

---

## **🚀 Getting Started**

### **🎯 Prerequisites**
- Java 23+
- Maven 3.x+
- Minio Server (v8.5.15+)

### **1️⃣ Install Minio**
Download and install Minio from [here](https://min.io/download).  
Run the server locally:  

```bash
minio server /data
```

### **2️⃣ Clone Repository**
```bash
git clone https://github.com/ShousenZHANG/minio-unit.git
cd Minio_test
```

### **3️⃣ Configure Credentials**
Edit the `application.yml` file with your Minio credentials:  

```yaml
minio:
  # User-defined MinIO configuration
  # Replace the following values with your own MinIO server details
  endpoint: http://localhost:9000  # MinIO server address
  accessKey: your-access-key  # Custom MinIO access key
  secretKey: your-secret-key  # Custom MinIO secret key (keep it secure)
```

### **4️⃣ Build & Run**
```bash
mvn clean install
mvn spring-boot:run
```

🎉 Visit `http://localhost:8080` to start using the APIs!

---

## **💼 API Endpoints**

### **File Operations**
| Endpoint                 | Method | Description          |
| ------------------------ | ------ | -------------------- |
| `/upload`               | POST   | Upload multiple files to MinIO |
| `/deleteObject`         | DELETE | Delete a file from MinIO |
| `/downloadToLocal`      | POST   | Download a file to the local disk |
| `/downloadFile`         | POST   | Download a file using a stream |
| `/listObjects`          | GET    | List all file information in a bucket |

### **Bucket Operations**
| Endpoint                | Method | Description         |
| ----------------------- | ------ | ------------------- |
| `/makeBucket`          | POST   | Create a new bucket |
| `/listBuckets`         | GET    | List all buckets    |
| `/deleteBucket`        | DELETE | Delete a bucket     |
| `/getBucketPolicy`     | GET    | Retrieve bucket policy |
| `/getObjectUrl`        | POST   | Generate a download URL |
| `/getUploadUrl`        | POST   | Generate an upload URL |

---

## **🤝 Contributing**  

We ❤️ contributions! Follow these steps:  

1. Fork this repository.  

2. Create a new feature branch:  

   ```bash
   git checkout -b feature-name
   ```

3. Commit your changes:  

   ```bash
   git commit -m "Add feature-name"
   ```

4. Push and open a pull request.  

---

## **🗉 License**  

This project is licensed under the MIT License.  

---

## **📧 Contact**  

👤 **Eddy ZHANG**  
📧 Email: [eddy.zhang24@gmail.com](mailto:eddy.zhang24@gmail.com)  

