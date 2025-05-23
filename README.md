# 🎬 Screenmatch

Screenmatch is a Java Spring Boot application that fetches data from the [OMDb API](https://www.omdbapi.com/) and allows users to explore TV series, view season ratings, and display the top 5 highest-rated episodes.

## 📌 Features

- 🔍 Search for a TV show by title
- 📈 View average ratings for each season
- ⭐ Display the top 5 episodes with the highest ratings
- 🌐 Fetch data dynamically from OMDb API
- 🧾 JSON deserialization using Jackson
- 🚀 Stream-based filtering, sorting, and aggregation of episode data

## 🛠️ Technologies Used

- Java 17+
- Spring Boot
- Jackson (for JSON parsing)
- Java Streams & Lambda Expressions
- OMDb API

## 🧩 Project Structure



## ▶️ How to Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/viniciusberger/screenmatch.git
   cd screenmatch


## Sample output
Type a show: 
Breaking Bad

1 - 8.3
2 - 9.1
...

Top 5 Episodes:
Ozymandias - 10.0 - Season 5
Face Off - 9.9 - Season 4
...

