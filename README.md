# 🌍 WorldMapJava
**Global Disease Monitoring and Analysis System**

A full-stack Java desktop application designed to track and analyze the global spread of diseases. It features a user-friendly Java Swing interface connected to a MySQL database, allowing users to visualize and interact with disease data on a world map in real-time.

---

## 🔧 Features

- 🔐 **Authentication System** with role-based access (`Admin`, `Analyst`, `Guest`)
- 🗺️ **Interactive World Map** using JXMapViewer with real-time statistics by country (cases, deaths, recoveries)
- 📊 **Dynamic Statistics Dashboard** with filters by disease, country, and time period
- 🛠️ **Admin Panel** for managing diseases, countries, cases, and users
- 📁 **CSV Import/Export** for bulk data handling
- 🗃️ **MySQL Backend** with a structured relational schema (`diseases`, `countries`, `cases`, `users`, `reports`)
- 💡 **Object-Oriented Design** for clean, maintainable, and scalable architecture

> **Built With:**  
> Java Swing · MySQL · JDBC · JXMapViewer

---

## 🧪 Try It Yourself

You can import the database schema into **MySQL Workbench** and try the application locally.

### 🗂 Database
- **Database Name:** `DiseaseSystemDB`
- Import the provided `.sql` file into your MySQL environment

### 🔐 Test Login Credentials

| Role    | Username       | Password  |
|---------|----------------|-----------|
| Admin   | `user_admin`   | `user123` |
| Analyst | `user_analyst` | `user123` |
| Guest   | `user_guest`   | `user123` |

Each user role provides different levels of access and functionality within the app.

---

## 📥 CSV Import Note

To test the **"Import CSV"** button (available for Admin and Analyst roles only), try uploading the sample file:  
📄 `ImportExample.csv`

This allows bulk insertion of disease case records into the database.

---

## 🙏 Thank You!
 
If you encounter any issues or have suggestions, feel free to contribute or reach out!
