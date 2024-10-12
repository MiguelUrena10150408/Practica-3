CREATE TABLE IF NOT EXISTS producto (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(255),
    price DOUBLE,
    stock INT
    );

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    password VARCHAR(255),
    is_admin BOOLEAN
    );

CREATE TABLE IF NOT EXISTS compra (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      user_id INT,
                                      total DOUBLE,
                                      FOREIGN KEY (users_id) REFERENCES users(id)
    );
