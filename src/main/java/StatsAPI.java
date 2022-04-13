import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StatsAPI {
    public static void main(String[] args) throws IOException {
        String rawJson = Files.readString(Paths.get("data.json"));
        try {
            JSONArray jsonArray = new JSONArray(rawJson);

            /*Convert Raw Json to ArrayList for easy manipulation*/
            ArrayList<JSONObject> rawArrayList = convertToArrayList(jsonArray);
            System.out.println("Raw Json: " + jsonArray);
            System.out.println("RawCount: " + jsonArray.length() + "\n\n");

            List<JSONObject> uniqueWithCount = listUniqueRecipeOccurrences(rawArrayList);

            String[] filterList = {"Potato", "Veggie", "Mushroom"};

            JSONObject finalJson = new JSONObject();
            finalJson.put("unique_recipe_count", uniqueWithCount.size());
            finalJson.put("count_per_recipe", generateCountPerRecipe(uniqueWithCount));
            finalJson.put("busiest_postcode", generateBusiestPostcode(uniqueWithCount).get(0));
            finalJson.put("match_by_name", filterByMatchList(uniqueWithCount, filterList));

            System.out.println("Final Expected Output: " + finalJson);
        } catch (Exception error) {
            System.out.println("Please add data to your file: " + error);
        }
    }

    public static ArrayList<JSONObject> convertToArrayList(JSONArray array) {
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        for (Object object : array) {
            if (object instanceof JSONObject) {
                arrayList.add((JSONObject) object);
            }
        }

        return arrayList;
    }

    /*Generate count per recipe from input unique sorted list - sorted by count of occurrences*/
    private static JSONArray generateCountPerRecipe(List<JSONObject> uniqueWithCount) {
        JSONArray outputJson = new JSONArray();
        for (JSONObject jsonObject : uniqueWithCount) {
            JSONObject newJson = new JSONObject();
            newJson.put("recipe", jsonObject.getString("recipe"));
            newJson.put("count", jsonObject.getInt("count"));
            outputJson.put(newJson);
        }

        return outputJson;
    }

    /*Generate busiest postcode from input unique sorted list - sorted by count of occurrences*/
    private static ArrayList<JSONObject> generateBusiestPostcode(List<JSONObject> uniqueWithCount) {
        List<JSONObject> sortedUniqueRecipeList = sortUniqueRecipeList(uniqueWithCount, true);
        ArrayList<JSONObject> outputJson = new ArrayList<>();
        for (JSONObject jsonObject : sortedUniqueRecipeList) {
            JSONObject newJson = new JSONObject();
            newJson.put("postcode", jsonObject.getString("postcode"));
            newJson.put("delivery_count", jsonObject.getInt("count"));
            outputJson.add(newJson);
        }

        return outputJson;
    }

    /*Generate a sorted unique list & add a count of occurrences to each item*/
    public static List<JSONObject> listUniqueRecipeOccurrences(ArrayList<JSONObject> rawList) {
        List<JSONObject> uniqueList = sortUniqueRecipeList(rawList, false);
        for (JSONObject jsonObject : uniqueList) {
            List<JSONObject> filtered = rawList.stream()
                    .filter(c -> c.getString("recipe").equals(jsonObject.getString("recipe")))
                    .collect(Collectors.toList());
            jsonObject.put("count", filtered.size());
        }

        return uniqueList;
    }

    /*Generate a list of recipes filtered from input unique sorted list*/
    public static List<String> filterByMatchList(List<JSONObject> uniqueList, String[] matchList) {
        List<String> outputList = new ArrayList<>();
        for (JSONObject jsonObject : uniqueList) {
            for (String s : matchList) {
                if (jsonObject.getString("recipe").contains(s)) outputList.add(jsonObject.getString("recipe"));
            }
        }

        return outputList;
    }

    /*Utility function to sort the unique list by either count or recipe name*/
    public static List<JSONObject> sortUniqueRecipeList(List<JSONObject> jsonList, boolean sortByCount) {
        List<JSONObject> inputList = new ArrayList<>(jsonList);

        if (sortByCount) inputList.sort((a, b) -> Integer.compare(b.getInt("count"), a.getInt("count")));
        else inputList.sort(Comparator.comparing(a -> a.getString("recipe")));

        List<JSONObject> outputList = new ArrayList<>();
        List<String> uniqueRecipeList = new ArrayList<>();
        for (JSONObject jsonObject : inputList) {
            if (!uniqueRecipeList.contains(jsonObject.getString("recipe"))) {
                uniqueRecipeList.add(jsonObject.getString("recipe"));
                outputList.add(jsonObject);
            }
        }

        return outputList;
    }
}