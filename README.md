# Compose Meal Vault (Android Native Architecture)

> This Android app coursework is built strictly with Jetpack Compose, focusing on native architecture with no third-party libraries. It is a Meal Search and Storage app featuring local data persistence using Room Database, web service integration with manual JSON parsing, and an advanced case-insensitive substring search for all locally stored meals.

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Room SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)

## Screenshots & Demo
*(Add a 2x2 grid of screenshots here showing the main search screen, the populated Room database list, the parsed web service results, and the meal details screen).*
MainScreen:
<img width="484" height="765" alt="image" src="https://github.com/user-attachments/assets/327c969a-caf7-4cb2-94fd-5200ffa5bb99" />
Searching By Ingredient Screen:
<img width="526" height="912" alt="image" src="https://github.com/user-attachments/assets/a4258c6f-9e5c-4a60-8256-3e90a2263d9c" />
Searching Database screen: 
<img width="490" height="872" alt="image" src="https://github.com/user-attachments/assets/400f4f62-6255-421c-bf3b-bd11e643e65b" />
Searching on WEB screen:
<img width="511" height="858" alt="image" src="https://github.com/user-attachments/assets/3f08b426-7d83-40d4-950d-43406dc213e9" />


## Features & Functionality

*   **Zero Third-Party Library Constraint:** Built entirely without industry-standard shortcuts (No Retrofit, Volley, Glide, Coil, or Gson). This project demonstrates a deep understanding of native Android SDKs.
*   **100% Declarative UI:** Interface and navigation constructed entirely with Jetpack Compose.
*   **Local Persistence (Room DB):** Robust local database that initializes with pre-populated file data and securely saves new meals fetched from the web.
*   **Native Web Service Integration:** Connects to external APIs using native networking protocols, featuring manual JSON parsing to map unstructured web responses to local Data Classes.
*   **Advanced Search Engine:** Features a real-time, case-insensitive substring search querying the Room Database directly.

## Tech Stack & Architecture

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Database:** Room (SQLite)
*   **Networking:** Native `HttpURLConnection` / `java.net`
*   **Asynchronous Processing:** Kotlin Coroutines (`Dispatchers.IO`)
*   **Data Parsing:** Native `org.json`

## Technical Challenges & Solutions

**Native Networking & Manual JSON Parsing**
*   **Challenge:** Modern Android development heavily relies on Retrofit and Gson for API calls. Building a robust network layer without these libraries required handling connection timeouts, background threading, and manual data mapping without crashing the main UI thread.
*   **Solution:** Engineered a custom network client utilizing `HttpURLConnection` wrapped in Kotlin Coroutines (`Dispatchers.IO`). I manually mapped the incoming `InputStream` to `JSONObject`/`JSONArray` structures, carefully extracting the required fields to populate my Kotlin data classes while ensuring UI state remained reactive.

**Asynchronous Image Loading without Glide/Coil**
*   **Challenge:** Loading images from web URLs in Jetpack Compose usually requires Coil. Without it, downloading image data on the main thread would freeze the UI and cause Application Not Responding (ANR) errors.
*   **Solution:** Developed a custom composable image loader. I utilized Coroutines to fetch the image bytes asynchronously via native networking streams, decoded them using `BitmapFactory`, and then hoisted the resulting `ImageBitmap` into Compose State to trigger a seamless UI update once the download completed.

**Optimized Substring Database Search**
*   **Challenge:** Implementing a search that updates the UI dynamically as the user types, while remaining case-insensitive and matching substrings anywhere in the meal name.
*   **Solution:** Leveraged Room Database's `@Query` capabilities using SQLite `LIKE '%' || :searchQuery || '%'` syntax. By exposing this query as a reactive stream (like Kotlin `Flow` or `LiveData`) to the Compose UI, the application instantly filters local database records with high performance.

*   Author
Vinicius Carvalho Hezin
