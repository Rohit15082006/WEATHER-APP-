
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherApp2 {
    // API and configuration constants
    private static final String API_KEY = "13828b8798daaef3ccba7c6b8cbb55fe"; // Replace with your OpenWeather API key
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static final String JSON_FILE = "weather_data.json";

    // Mapping of keywords to symbols
    private static final Map<String, String> SUGGESTION_SYMBOLS = new HashMap<>() {{
        // Food and Drink
        put("eat", "🍽️");
        put("food", "🍔");
        put("cook", "🥘");
        put("restaurant", "🍣");
        put("drink", "🍹");
        put("coffee", "☕");

        // Activities
        put("walk", "🚶");
        put("run", "🏃");
        put("exercise", "💪");
        put("gym", "🏋️");
        put("bike", "🚲");
        put("swim", "🏊");
        put("hike", "🥾");

        // Work and Study
        put("work", "💼");
        put("study", "📚");
        put("meeting", "👥");
        put("project", "📊");
        put("report", "📝");

        // Home and Chores
        put("clean", "🧹");
        put("laundry", "🧺");
        put("groceries", "🛒");
        put("garden", "🌱");

        // Entertainment and Leisure
        put("movie", "🎬");
        put("music", "🎵");
        put("read", "📖");
        put("game", "🎮");
        put("shop", "🛍️");
        put("art", "🎨");

        // Travel and Outdoors
        put("travel", "✈️");
        put("trip", "🧳");
        put("vacation", "🏖️");
        put("camp", "⛺");

        // Personal Care
        put("sleep", "😴");
        put("relax", "🧘");
        put("health", "❤️");
        put("doctor", "🩺");

        // Technology
        put("computer", "💻");
        put("phone", "📱");
        put("code", "💻");
        put("email", "📧");

        // General
        put("plan", "📅");
        put("buy", "🛒");
        put("go", "🚀");
        put("check", "✅");
    }};

    // Default symbols for when no keyword match is found
    private static final String[] DEFAULT_SYMBOLS = {
            "🌈", "🌟", "🍀", "🚀", "🔮", "🎲", "🧩", "🌻",
            "🦄", "🍁", "🌍", "🎈", "🦋", "🍄", "🎭"
    };

    // Swing components
    private JFrame frame;
    private JTextField cityField;
    private JTextArea weatherArea;
    private DefaultListModel<String> suggestionsListModel;
    private JList<String> suggestionsList;
    
    // To-Do List components
    private DefaultListModel<String> todoListModel;
    private JList<String> todoList;
    private JTextField todoInputField;
    
    private Random random;

    // Constructor
    @SuppressWarnings("unused")
    public WeatherApp2() {
        random = new Random();

        // Create main frame
        frame = new JFrame("Weather App");
        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        Font customFont = new Font("Segoe UI Emoji", Font.PLAIN, 18);

        // Top panel with city input
        JPanel topPanel = new JPanel();
        cityField = new JTextField(15);
        JButton getWeatherButton = new JButton("Get Weather");
        topPanel.add(new JLabel("Enter Location:"));
        topPanel.add(cityField);
        topPanel.add(getWeatherButton);

        // Weather display area
        weatherArea = new JTextArea();
        weatherArea.setFont(customFont);
        weatherArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(weatherArea);

        // Create a split panel for suggestions and to-do list
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel suggestionsPanel = createSuggestionsPanel();
        JPanel todoPanel = createTodoPanel();
        splitPane.setLeftComponent(suggestionsPanel);
        splitPane.setRightComponent(todoPanel);
        splitPane.setDividerLocation(0.5); // Equal split

        // Add components to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(splitPane, BorderLayout.SOUTH);

        // Action listeners
        getWeatherButton.addActionListener(e -> getWeather(cityField.getText().trim()));

        // Load existing data
        loadSuggestionsFromJSON();
        loadTodoItemsFromJSON();

        // Make frame visible
        frame.setVisible(true);
    }

    // Create suggestions panel
    @SuppressWarnings("unused")
    private JPanel createSuggestionsPanel() {
        JPanel suggestionsPanel = new JPanel(new BorderLayout());
        suggestionsListModel = new DefaultListModel<>();
        suggestionsList = new JList<>(suggestionsListModel);
        suggestionsList.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JScrollPane suggestionsScroll = new JScrollPane(suggestionsList);

        // Remove suggestion button
        JButton removeSuggestionButton = new JButton("Remove Suggestion");
        removeSuggestionButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        // Suggestions control panel
        JPanel suggestionsControlPanel = new JPanel();
        suggestionsControlPanel.add(removeSuggestionButton);

        // Suggestions label
        JLabel suggestionsLabel = new JLabel("Weather-Based Suggestions:");
        suggestionsLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        // Arrange suggestions panel
        suggestionsPanel.add(suggestionsLabel, BorderLayout.NORTH);
        suggestionsPanel.add(suggestionsScroll, BorderLayout.CENTER);
        suggestionsPanel.add(suggestionsControlPanel, BorderLayout.SOUTH);

        // Action listeners
        removeSuggestionButton.addActionListener(e -> removeSuggestion(suggestionsList.getSelectedIndex()));

        return suggestionsPanel;
    }

    // Create To-Do panel
    @SuppressWarnings("unused")
    private JPanel createTodoPanel() {
        JPanel todoPanel = new JPanel(new BorderLayout());
        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);
        todoList.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JScrollPane todoScrollPane = new JScrollPane(todoList);

        // To-Do input panel
        JPanel todoInputPanel = new JPanel(new FlowLayout());
        todoInputField = new JTextField(20);
        JButton addTodoButton = new JButton("Add To-Do");
        addTodoButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JButton removeTodoButton = new JButton("Remove To-Do");
        removeTodoButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        todoInputPanel.add(new JLabel("To-Do:"));
        todoInputPanel.add(todoInputField);
        todoInputPanel.add(addTodoButton);
        todoInputPanel.add(removeTodoButton);

        // To-Do panel label
        JLabel todoLabel = new JLabel("My To-Do List:");
        todoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        // Arrange To-Do panel
        todoPanel.add(todoLabel, BorderLayout.NORTH);
        todoPanel.add(todoScrollPane, BorderLayout.CENTER);
        todoPanel.add(todoInputPanel, BorderLayout.SOUTH);

        // Add action listeners for To-Do list
        addTodoButton.addActionListener(e -> addTodoItem());
        removeTodoButton.addActionListener(e -> removeTodoItem());

        return todoPanel;
    }

    // Method to find the most relevant symbol for a suggestion
    @SuppressWarnings("unused")
    private String findMostRelevantSymbol(String suggestion) {
        // Convert suggestion to lowercase for case-insensitive matching
        String lowerSuggestion = suggestion.toLowerCase();

        // Check for keyword matches
        for (Map.Entry<String, String> entry : SUGGESTION_SYMBOLS.entrySet()) {
            if (lowerSuggestion.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // If no match found, return a random default symbol
        return DEFAULT_SYMBOLS[random.nextInt(DEFAULT_SYMBOLS.length)];
    }

    // Remove a suggestion from the list
    private void removeSuggestion(int index) {
        if (index != -1) {
            suggestionsListModel.remove(index);
            saveSuggestionsToJSON();
        }
    }

    // Add To-Do item method
    private void addTodoItem() {
        String newTodo = todoInputField.getText().trim();
        if (!newTodo.isEmpty()) {
            todoListModel.addElement("☐ " + newTodo);
            todoInputField.setText(""); // Clear the input field
            saveTodoItemsToJSON();
        }
    }

    // Remove To-Do item method
    private void removeTodoItem() {
        int selectedIndex = todoList.getSelectedIndex();
        if (selectedIndex != -1) {
            todoListModel.remove(selectedIndex);
            saveTodoItemsToJSON();
        }
    }

    // Fetch weather data for a given city
    private void getWeather(String city) {
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a location!");
            return;
        }

        try {
            String currentWeather = fetchWeatherData(city, "weather");
            String forecast = fetchWeatherData(city, "forecast");
            String airPollution = fetchAirPollution(city);

            String weatherInfo = "📍 Location: " + city + "\n\n" + currentWeather + "\n" + forecast + "\n" + airPollution;
            weatherArea.setText(weatherInfo);

            // Generate suggestions based on current weather
            generateWeatherSuggestions(currentWeather);

            saveWeatherToJSON(city, currentWeather, forecast, airPollution);

        } catch (Exception e) {
            weatherArea.setText("Error fetching weather data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generate weather-based suggestions
    private void generateWeatherSuggestions(String currentWeather) {
        // Clear previous suggestions
        suggestionsListModel.clear();

        // Parse weather conditions from the currentWeather string
        String lowercaseWeather = currentWeather.toLowerCase();

        // Check for different weather conditions and temperature ranges
        if (lowercaseWeather.contains("rain") || lowercaseWeather.contains("shower")) {
            suggestionsListModel.addElement("☔ Bring an umbrella");
            suggestionsListModel.addElement("🧥 Wear waterproof clothing");
            suggestionsListModel.addElement("🏠 Indoor activities recommended");
        }

        if (lowercaseWeather.contains("snow")) {
            suggestionsListModel.addElement("🧤 Wear warm gloves and hat");
            suggestionsListModel.addElement("🧣 Dress in layers for warmth");
            suggestionsListModel.addElement("⛄ Good day for winter activities");
        }

        if (lowercaseWeather.contains("clear") || lowercaseWeather.contains("sunny")) {
            suggestionsListModel.addElement("😎 Wear sunglasses and sunscreen");
            suggestionsListModel.addElement("🏖️ Great day for outdoor activities");
            suggestionsListModel.addElement("🥤 Stay hydrated");
        }

        if (lowercaseWeather.contains("cloud")) {
            suggestionsListModel.addElement("📸 Good lighting for photography");
            suggestionsListModel.addElement("🚶 Pleasant day for walking");
        }

        if (lowercaseWeather.contains("fog") || lowercaseWeather.contains("mist")) {
            suggestionsListModel.addElement("🚗 Drive carefully - reduced visibility");
            suggestionsListModel.addElement("🔦 Use fog lights when driving");
        }

        // Temperature-based suggestions
        if (lowercaseWeather.contains("temperature")) {
            try {
                // Extract temperature from string like "🌡 Temperature: 25.0°C"
                int startIndex = lowercaseWeather.indexOf("temperature:") + 12;
                int endIndex = lowercaseWeather.indexOf("°c", startIndex);
                String tempStr = lowercaseWeather.substring(startIndex, endIndex).trim();
                double temperature = Double.parseDouble(tempStr);

                if (temperature > 30) {
                    suggestionsListModel.addElement("🧊 Stay cool and hydrated");
                    suggestionsListModel.addElement("🏊 Swimming might be refreshing");
                    suggestionsListModel.addElement("🔆 Avoid direct sun during peak hours");
                } else if (temperature > 20) {
                    suggestionsListModel.addElement("👕 Light clothing recommended");
                    suggestionsListModel.addElement("🌳 Perfect weather for outdoor dining");
                } else if (temperature > 10) {
                    suggestionsListModel.addElement("🧥 Light jacket recommended");
                } else if (temperature > 0) {
                    suggestionsListModel.addElement("🧣 Bundle up with layers");
                    suggestionsListModel.addElement("☕ Good day for hot beverages");
                } else {
                    suggestionsListModel.addElement("❄️ Dress very warmly");
                    suggestionsListModel.addElement("🧤 Don't forget gloves and hat");
                    suggestionsListModel.addElement("🔥 Indoor heating essential");
                }
            } catch (Exception e) {
                // If temperature parsing fails, add a generic suggestion
                suggestionsListModel.addElement("📱 Check forecast before heading out");
            }
        }

        // Air quality suggestions
        if (lowercaseWeather.contains("pm2.5") || lowercaseWeather.contains("air pollution")) {
            suggestionsListModel.addElement("😷 Consider mask if sensitive to pollution");
            suggestionsListModel.addElement("🏠 Limit outdoor activities if AQI is high");
        }

        // Wind-based suggestions
        if (lowercaseWeather.contains("wind")) {
            try {
                // Extract wind speed from string like "🌬 Wind: 5.1 m/s"
                int startIndex = lowercaseWeather.indexOf("wind:") + 5;
                int endIndex = lowercaseWeather.indexOf("m/s", startIndex);
                String windStr = lowercaseWeather.substring(startIndex, endIndex).trim();
                double windSpeed = Double.parseDouble(windStr);

                if (windSpeed > 10) {
                    suggestionsListModel.addElement("💨 Secure loose outdoor items");
                    suggestionsListModel.addElement("🪁 Great day for flying kites");
                } else if (windSpeed > 5) {
                    suggestionsListModel.addElement("🧢 Wear a hat to prevent it blowing away");
                }
            } catch (Exception e) {
                // If wind parsing fails, add a generic suggestion
                suggestionsListModel.addElement("🌬️ Be mindful of wind conditions");
            }
        }

        // Add some general suggestions if none were added
        if (suggestionsListModel.isEmpty()) {
            suggestionsListModel.addElement("📱 Check weather updates regularly");
            suggestionsListModel.addElement("🌍 Dress appropriately for the conditions");
            suggestionsListModel.addElement("☂️ Be prepared for weather changes");
        }

        // Save the generated suggestions
        saveSuggestionsToJSON();
    }

    // Fetch weather data from OpenWeatherMap API
    private String fetchWeatherData(String city, String type) throws Exception {
        String urlString = BASE_URL + type + "?q=" + city + "&units=metric&appid=" + API_KEY;
        JSONObject json = getJSONResponse(urlString);

        if (type.equals("weather")) {
            JSONObject main = json.getJSONObject("main");
            JSONArray weatherArray = json.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            JSONObject wind = json.getJSONObject("wind");

            return String.format("🌡 Temperature: %.1f°C\n💧 Humidity: %d%%\n🌬 Wind: %.1f m/s\n🌤 Condition: %s\n",
                    main.getDouble("temp"), main.getInt("humidity"), wind.getDouble("speed"), weather.getString("description"));
        } else {
            JSONArray list = json.getJSONArray("list");
            StringBuilder forecast = new StringBuilder("📅 5-Day Forecast:\n");
            for (int i = 0; i < 5; i++) {
                JSONObject day = list.getJSONObject(i * 8);
                JSONObject main = day.getJSONObject("main");
                JSONArray weatherArray = day.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);

                forecast.append(String.format("📆 %s: 🌡 %.1f°C - %s\n",
                        day.getString("dt_txt"), main.getDouble("temp"), weather.getString("description")));
            }
            return forecast.toString();
        }
    }

    // Fetch air pollution data
    private String fetchAirPollution(String city) throws Exception {
        String coordsUrl = BASE_URL + "weather?q=" + city + "&appid=" + API_KEY;
        JSONObject coordsJson = getJSONResponse(coordsUrl);
        JSONObject coord = coordsJson.getJSONObject("coord");
        double lat = coord.getDouble("lat");
        double lon = coord.getDouble("lon");

        String urlString = BASE_URL + "air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;
        JSONObject json = getJSONResponse(urlString);
        JSONObject components = json.getJSONArray("list").getJSONObject(0).getJSONObject("components");

        return String.format("\n🌍 Air Pollution Data:\nCO: %.2f µg/m³\nNO₂: %.2f µg/m³\nSO₂: %.2f µg/m³\nPM2.5: %.2f µg/m³\n",
                components.getDouble("co"), components.getDouble("no2"), components.getDouble("so2"), components.getDouble("pm2_5"));
    }

    // Save weather data to JSON file
    private void saveWeatherToJSON(String city, String currentWeather, String forecast, String airPollution) {
        try {
            JSONObject data = new JSONObject();
            data.put("Location", city);
            data.put("currentWeather", currentWeather);
            data.put("forecast", forecast);
            data.put("airPollution", airPollution);

            // Save suggestions
            JSONArray suggestions = new JSONArray();
            for (int i = 0; i < suggestionsListModel.size(); i++) {
                suggestions.put(suggestionsListModel.get(i));
            }
            data.put("suggestions", suggestions);

            // Save To-Do items
            JSONArray todoItems = new JSONArray();
            for (int i = 0; i < todoListModel.size(); i++) {
                todoItems.put(todoListModel.get(i));
            }
            data.put("todoItems", todoItems);

            FileWriter file = new FileWriter(JSON_FILE);
            file.write(data.toString(4));
            file.flush();
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save suggestions to JSON file
    private void saveSuggestionsToJSON() {
        try {
            File file = new File(JSON_FILE);
            JSONObject json;

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder jsonText = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonText.append(line);
                }
                reader.close();
                json = new JSONObject(jsonText.toString());
            } else {
                json = new JSONObject();
            }

            JSONArray suggestions = new JSONArray();
            for (int i = 0; i < suggestionsListModel.size(); i++) {
                suggestions.put(suggestionsListModel.get(i));
            }
            json.put("suggestions", suggestions);

            // Save To-Do items
            JSONArray todoItems = new JSONArray();
            for (int i = 0; i < todoListModel.size(); i++) {
                todoItems.put(todoListModel.get(i));
            }
            json.put("todoItems", todoItems);

            FileWriter fileWriter = new FileWriter(JSON_FILE);
            fileWriter.write(json.toString(4));
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save To-Do items to JSON
    private void saveTodoItemsToJSON() {
        try {
            File file = new File(JSON_FILE);
            JSONObject json;

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder jsonText = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonText.append(line);
                }
                reader.close();
                json = new JSONObject(jsonText.toString());
            } else {
                json = new JSONObject();
            }

            // Save To-Do items
            JSONArray todoItems = new JSONArray();
            for (int i = 0; i < todoListModel.size(); i++) {
                todoItems.put(todoListModel.get(i));
            }
            json.put("todoItems", todoItems);

            FileWriter fileWriter = new FileWriter(JSON_FILE);
            fileWriter.write(json.toString(4));
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load suggestions from JSON file
    private void loadSuggestionsFromJSON() {
        try {
            File file = new File(JSON_FILE);
            if (!file.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(jsonText.toString());
            JSONArray suggestions = json.optJSONArray("suggestions");

            if (suggestions != null) {
                for (int i = 0; i < suggestions.length(); i++) {
                    suggestionsListModel.addElement(suggestions.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load To-Do items from JSON
    private void loadTodoItemsFromJSON() {
        try {
            File file = new File(JSON_FILE);
            if (!file.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(jsonText.toString());
            JSONArray todoItems = json.optJSONArray("todoItems");

            if (todoItems != null) {
                for (int i = 0; i < todoItems.length(); i++) {
                    todoListModel.addElement(todoItems.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get JSON response from API
    @SuppressWarnings("deprecation")
    private JSONObject getJSONResponse(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return new JSONObject(response.toString());
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherApp2::new);
    }
}