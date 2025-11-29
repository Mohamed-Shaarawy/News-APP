# News APP

An Android application built with **Kotlin** that integrates with **NewsAPI.org** to display top headlines and search results. The app supports **country & category-based news**, **search with sorting**, **Google Sign-In**, **per-user favorites stored in Firestore**, and **local notifications** when new headlines are loaded.


---

## Features

- **Top Headlines by Country & Category**
  - View top headlines for a selected country and category.
  - Country selection via a RecyclerView of country cards.
  - Category selection via a RecyclerView of category cards.

- **Search News**
  - Search for articles by a query term.
  - Sort search results by:
    - `relevancy`
    - `popularity`
    - `publishedAt`

- **Rich Article Details**
  - List screen (RecyclerView) with:
    - Thumbnail image
    - Title
    - Short description
    - Published date
    - Source
  - Details screen shows:
    - Full title & description
    - Published date & source
    - Content & URL
    - Full-size image (Glide with placeholders).

- **Favorites per User (Countries & Categories)**
  - Add favorite **countries** and **categories**.
  - Favorites are stored per user in **Firebase Firestore**.
  - On load, favorites are fetched from Firestore and rendered.

- **Google Sign-In (Credential Manager)**
  - Sign in via Google using the Android **Credential Manager** APIs.
  - On first login, user profile is written into Firestore (`users` collection).
  - On subsequent logins, existing user data is reused.

- **Local Notifications**
  - When new headlines are successfully fetched, the app triggers a **system notification**:
    - Title: “News App”
    - Text: “New Headlines are available, check them out”

- **Modern Android Stack**
  - Single-activity, multi-fragment architecture with Navigation Component (Safe Args).
  - Retrofit + OkHttp + Gson for network and JSON.
  - Glide for image loading.
  - Firebase Auth + Firestore for auth and user data.

---

##  Project Structure

```text
News-APP/
├─ .gitignore
├─ .idea/                            # Android Studio / IntelliJ project files
├─ app/
│  ├─ .gitignore
│  ├─ build.gradle.kts               # Module-level Gradle (Kotlin DSL)
│  ├─ proguard-rules.pro
│  └─ src/
│     └─ main/
│        ├─ java/
│        │  └─ com/example/newsapp/
│        │     ├─ MainActivity.kt
│        │     ├─ GoogleSignInHelper.kt
│        │     ├─ CategoriesAdapter.kt
│        │     ├─ CategoriesListener.kt
│        │     ├─ CountriesAdapter.kt
│        │     ├─ CountryListener.kt
│        │     ├─ NewsAdapter.kt
│        │     ├─ SigninFragment.kt
│        │     ├─ SignupFragment.kt
│        │     ├─ NewsRecyclerFragment.kt
│        │     ├─ SearchResultsFragment.kt
│        │     ├─ NewsDetailsFragment.kt
│        │     ├─ FavouriteHeadlinesFragment.kt
│        │     ├─ api/
│        │     │  └─ NewsAPIService.kt
│        │     └─ model/
│        │        ├─ NewsResponse.kt
│        │        ├─ NewsPost.kt
│        │        └─ Source.kt
│        └─ res/
│           ├─ drawable/             # icons, shapes, placeholders (e.g., imageloadfailed)
│           ├─ layout/               # activity_main, card_design, fragment_*.xml, dialogs, etc.
│           ├─ menu/                 # bottom navigation and other menus
│           ├─ mipmap-anydpi-v26/
│           ├─ mipmap-hdpi/
│           ├─ mipmap-mdpi/
│           ├─ mipmap-xhdpi/
│           ├─ mipmap-xxhdpi/
│           ├─ mipmap-xxxhdpi/       # launcher icons
│           ├─ navigation/           # navigation graph(s) for fragments (Safe Args)
│           ├─ values-night/         # dark theme resources
│           ├─ values/               # colors, strings, styles, themes, arrays (e.g. sort_options)
│           └─ xml/                  # misc configurations (e.g., network_security_config)
├─ build.gradle.kts                  # Root Gradle script
├─ gradle.properties                 # Gradle and Android configuration flags
├─ gradle/                           # Gradle wrapper support files
├─ gradlew
├─ gradlew.bat
├─ settings.gradle.kts               # Declares modules, plugin management
└─ README.md
```

---

## 🧩 Core Architecture

### Data Models (`com.example.newsapp.model`)

- `Source`
  - Fields: `id: String?`, `name: String`
- `NewsPost`
  - Fields: `source: Source`, `author`, `title`, `description`, `url`, `urlToImage`, `publishedAt`, `content`
- `NewsResponse`
  - Root object for NewsAPI responses:
    - `status: String`
    - `totalResults: Int`
    - `articles: List<NewsPost>`

These are the types used by Retrofit + Gson to parse responses from NewsAPI.

---

### UI Flow

- **`MainActivity`**
  - Hosts all fragments.
  - Implements `startNotification(context)` to show a notification after top headlines load.

- **Authentication & Entry**
  - `SigninFragment`
    - Google Sign-In button using `GoogleSignInHelper`.
    - On success, navigates to `NewsRecyclerFragment`.
  - `SignupFragment`
    - Basic fields and a button to navigate to `SigninFragment`.

- **Headlines & Search**
  - `NewsRecyclerFragment`
    - Takes optional `countryName` and `categoryName` args (Safe Args).
    - Fetches top headlines via `NewsAPIService.getTopNews`.
    - Shows articles in a `RecyclerView` backed by `NewsAdapter`.
    - Search bar + button navigate to `SearchResultsFragment` with a `searchQuery` arg.
    - Bottom nav item navigates to `FavouriteHeadlinesFragment`.
    - Triggers `MainActivity.startNotification(requireContext())` after successful fetch.

  - `SearchResultsFragment`
    - Receives `searchQuery`.
    - Uses a Spinner to choose sort order:
      - `relevancy`, `popularity`, `publishedAt`
    - Calls `NewsAPIService.sort` with the chosen `sortBy`.
    - Displays results in a `RecyclerView` with `NewsAdapter`.

  - `NewsDetailsFragment`
    - Receives full article details via Safe Args.
    - Displays text fields (title, description, date, source, content, URL).
    - Loads image using Glide with placeholder/error image.
    - Return button navigates back to the news list.

- **Favorites**
  - `FavouriteHeadlinesFragment`
    - Manages favorite countries and categories:
      - Countries: `CountriesAdapter` + `CountryListener`
      - Categories: `CategoriesAdapter` + `CategoriesListener`
    - Add dialogs for both:
      - Country dialog (`dialog_view`)
      - Category dialog (`category_dialog_view`)
    - Saves favorites in Firestore under `users/{uid}` in fields:
      - `countries: [String]`
      - `categories: [String]`
    - Loads favorites at startup and refreshes adapters.
    - On country click:
      - Calls `NewsAPIService.getFavCountries` and navigates to `NewsRecyclerFragment` with `countryName` and the current `categoryName`.
    - On category click:
      - Sets `categoryName` and navigates to `NewsRecyclerFragment`.

---

## 🌐 Networking & API Integration

All networking is done through **Retrofit** with **OkHttp** and **GsonConverterFactory**, using the interface:

```kotlin
package com.example.newsapp.api

import com.example.newsapp.model.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPIService {
    @GET("v2/top-headlines")
    fun getTopNews(
        @Query("country") country: String = "us",
        @Query("category") category: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>

    @GET("v2/everything")
    fun searchArticles(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>

    @GET("v2/everything")
    fun sort(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>

    @GET("v2/top-headlines")
    fun getFavCountries(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}
```

### Base Setup

The base URL and API key are defined in the fragments:

```kotlin
private const val BASE_URL = "https://newsapi.org/"
private const val API_Key = "YOUR_NEWS_API_KEY"
```

`NewsRecyclerFragment`, `SearchResultsFragment`, and `FavouriteHeadlinesFragment` all configure Retrofit with an `OkHttpClient` that injects a `User-Agent` header (required by NewsAPI):

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header(
                "User-Agent",
                "Mozilla/5.0 (Android 11; Mobile; rv:89.0) Gecko/89.0 Firefox/89.0"
            )
            .method(original.method(), original.body())
            .build()
        chain.proceed(request)
    }
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .build()
    .create(NewsAPIService::class.java)
```

> **Note:** For production, you should move `API_Key` out of source control (e.g., into `local.properties` or a backend service).

### Endpoints in Use

- **Top Headlines**
  - Used in `NewsRecyclerFragment`:
    ```kotlin
    retrofit.getTopNews(
        country = countryCode,
        category = categoryName,
        apiKey = API_Key
    )
    ```
  - Returns a `NewsResponse` whose `articles` are bound into the `NewsAdapter`.

- **Search (everything)**
  - You can call `searchArticles(query, apiKey)` if you want unsorted search results.
  - Currently, the app uses `sort` directly for search with sorting.

- **Search + Sort**
  - Used in `SearchResultsFragment`:
    ```kotlin
    retrofit.sort(
        query = searchQuery,
        sortBy = selectedSortOption, // "relevancy" | "popularity" | "publishedAt"
        apiKey = API_Key
    )
    ```

- **Favorite Countries Headlines**
  - Used in `FavouriteHeadlinesFragment` when a favorite country is selected:
    ```kotlin
    retrofit.getFavCountries(countryName, API_Key)
    ```
  - On success, navigates to `NewsRecyclerFragment` with that country and currently selected category.

---

## 🛠️ Technologies & Dependencies (High-Level)

- **Language**: Kotlin
- **Networking**: Retrofit, OkHttp, Gson
- **Image Loading**: Glide
- **Firebase**: FirebaseAuth, Firestore
- **Auth**: Google Sign-In with Credential Manager APIs
- **UI**: RecyclerView, CardView, Navigation Component, Fragments, Dialogs
- **Notifications**: `NotificationManager`, `NotificationChannel` (Android O+)

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Giraffe/Koala or newer)
- An Android emulator or physical device
- **NewsAPI.org** account and API key
- **Firebase** project set up with:
  - Authentication (Google)
  - Firestore

### Clone & Open

```bash
git clone https://github.com/Mohamed-Shaarawy/News-APP.git
cd News-APP
```

1. Open the project in Android Studio.
2. Let Gradle sync and indexing finish.

### Configure Keys & Firebase

1. **NewsAPI key**
   - Replace the `API_Key` constants in:
     - `NewsRecyclerFragment`
     - `SearchResultsFragment`
     - `FavouriteHeadlinesFragment`

2. **Firebase & Google Sign-In**
   - Add `google-services.json` to the `app/` module if not already present.
   - Ensure the `WEB_CLIENT_ID` in `GoogleSignInHelper` matches your OAuth client ID from the Firebase project.
   - Verify SHA-1 and package name configuration in Firebase/Google Cloud console.

Then run the app from Android Studio.

---

## 🔐 Security Notes

- Avoid committing real API keys and client IDs to public repos.
- Prefer using build-time injection (e.g., `local.properties`) or backend proxies.
- Configure Firestore security rules to lock user-specific data (e.g., only `users/{uid}` can read/write their document).

---


---

## 🗺️ Roadmap / Future Enhancements

- Offline cache with Room for recent headlines and search results.
- Favorite **articles** (not just countries/categories).
- Open articles in a WebView or Chrome Custom Tabs.
- ViewModel + Flow/LiveData refactor for better state management.
- Error & empty-state views on all fragments.
- Move API keys and secrets out of source code.

---



## 📝 License

Intended for **educational purposes only**
Copyright (c) 2025 Mohamed Shaarawy
