# アプリの説明
...

# ER Diagram
```mermaid
erDiagram
    Users {
        int Long PK
        String username
        String password
        String email
        LocalDateTime createdTime
    }

    Videos {
        int id PK
        int user_id FK
        string title
        string description
        string file_path
        string thumbnail_path
        int views
        datetime uploaded_at
    }

    VideoLikes {
        int id PK
        int video_id FK
        int user_id FK
        datetime liked_at
    }

    Users ||--o{ Videos : "uploads"
    Users ||--o{ VideoLikes : "likes"
    Videos ||--o{ VideoLikes : "liked by"
