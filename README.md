# 動画共有視聴システム
## 概要
...

## このアプリを使ってできること
...

# 技術スペック
## back-end framework : Java spring boot
...

## front-end : Thymeleaf template, bootstrap framework
...

## database : JPA, mysql
...

# 機能とサービス説明
...

## ログイン&会員登録
...

## 動画ホーム
...

## マイページ
...

## 動画登録
...

## 動画視聴
...

## いいですね ボタン(開発予定)
...

## 動画検索(開発予定)
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
