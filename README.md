# 🎥 動画共有視聴システム (Video Sharing & Streaming System)

## 概要
本プロジェクトは、ユーザーが動画をアップロードし、他のユーザーと共有・視聴できる動画共有プラットフォームです。Spring Bootを用いて開発されており、ユーザー認証、動画管理、動画視聴、いいね機能などを提供します。

### このアプリの主な機能
- **動画アップロード**: ユーザーが自身の動画をアップロード可能
- **動画視聴**: 他のユーザーが投稿した動画を視聴可能
- **ユーザー認証 & ログイン**: Spring Security による安全な会員管理
- **いいね機能**: ユーザーが動画に「いいね」できる機能
- **動画検索機能（開発予定）**: タイトルや説明で検索可能な機能追加予定

---

## 技術スタック
| 分類        | 技術                 |
|------------|--------------------|
| **言語**   | Java 17             |
| **フレームワーク** | Spring Boot, Spring Security, Spring Data JPA |
| **テンプレートエンジン** | Thymeleaf |
| **フロントエンド** | Bootstrap, JavaScript |
| **データベース** | MySQL |
| **ビルドツール** | Gradle |

---

## アーキテクチャ
```mermaid
graph TD;
    subgraph クライアント
        User["👤 ユーザー"]
        Browser["🌐 ブラウザ (Thymeleaf)"]
    end

    subgraph サーバー["Spring Boot サーバー"]
        Controller["コントローラー\n(リクエスト処理)"]
        Service["サービス\n(ビジネスロジック)"]
        Repository["リポジトリ\n(Spring Data JPA)"]
        Security["Spring Security\n(認証 & 認可)"]
    end

    subgraph データベース["Database"]
        MySQL["MySQL"]
    end

    User -->|リクエスト| Browser
    Browser -->|リクエスト| Controller
    Controller -->|処理| Service
    Service -->|データ取得| Repository
    Repository -->|DB クエリ| MySQL
    MySQL -->|データ返却| Repository
    Repository -->|データ返却| Service
    Service -->|レスポンス生成| Controller
    Controller -->|レスポンス送信| Browser
```

---

## データベース ERD
```mermaid
erDiagram
    USER {
        Long id PK "AUTO_INCREMENT"
        String userName "NOT NULL"
        String password "NOT NULL"
        String email "UNIQUE, NOT NULL"
        LocalDateTime createdTime "DEFAULT CURRENT_TIMESTAMP"
        String role
    }
    
    VIDEO {
        Long id PK "AUTO_INCREMENT"
        String title "NOT NULL"
        String description "NOT NULL"
        String filePath "NOT NULL"
        String fileUrl "NOT NULL"
        String thumbnailPath "NOT NULL"
        String thumbnailUrl "NOT NULL"
        Long views "DEFAULT 0"
        LocalDateTime uploadDate "DEFAULT CURRENT_TIMESTAMP"
        Long user_id FK "REFERENCES USER(id) NOT NULL"
    }
    
    VIDEOLIKE {
        Long id PK "AUTO_INCREMENT"
        Long video_id FK "REFERENCES VIDEO(id) NOT NULL"
        Long user_id FK "REFERENCES USER(id) NOT NULL"
        LocalDateTime likedTime "DEFAULT CURRENT_TIMESTAMP"
    }
    
    USER ||--o{ VIDEO : "1:N"
    USER ||--o{ VIDEOLIKE : "1:N"
    VIDEO ||--o{ VIDEOLIKE : "1:N"
```

---

## セットアップ & 実行方法
### **1️⃣ 環境構築**
- JDK 17 以上をインストール
- MySQL (DB: `video_db`) をセットアップ
- `application.yml` にデータベース情報を設定

### **アクセス**
- **アプリ URL:** `http://localhost:8080`
- **管理者ユーザー:** 初期設定なし（DB で直接登録）

---

## スクリーンショット
### 로그인 & 회원가입
このプラットフォームは必ず会員登録が必要です。ログインするために会員登録を行ってください。

<img width="1407" alt="Image" src="https://github.com/user-attachments/assets/04d92721-badf-48a2-a02d-78e8c8610491" />

会員登録が完了したら、ログインを行ってください。
[image]

### 홈화면
ログインすると、他のメンバーがアップロードした動画をすべて閲覧できます。
[image]

動画のタイトルをクリックすると、動画を視聴できます。
[image]

### 마이페이지
マイページでは、自分がアップロードした動画を管理できます。
[image]

動画をアップロードするには、動画ファイルをアップロードし、動画のタイトルと説明、サムネイル画像を設定します。[image]

動画をアップロードすると、「自分の動画」とホーム画面に自分の動画が表示されます。[

image]

動画を編集する際は、タイトル、説明、サムネイルのみ編集可能です。

[image]

---

## 今後のアップデート予定
- ✅ **検索機能**: タイトルや説明で動画を検索できるようにする
- ✅ **コメント機能**: 動画にコメントを追加できる機能


