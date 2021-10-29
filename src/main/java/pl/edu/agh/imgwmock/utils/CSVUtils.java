package pl.edu.agh.imgwmock.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.Polygon;
import pl.edu.agh.imgwmock.model.RiverPoint;
import pl.edu.agh.imgwmock.model.Station;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class CSVUtils {
    public static List<DailyPrecipitation> getDailyPrecipitationListFromCSV(String pathToFile) {
        List<DailyPrecipitation> dailyPrecipitations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
                    DailyPrecipitation dailyPrecipitation = new DailyPrecipitation(
                            Long.parseLong(record[0]),
                            record[1],
                            LocalDate.of(Integer.parseInt(record[2]), Integer.parseInt(record[3]), Integer.parseInt(record[4])),
                            Double.parseDouble(record[5])
                    );
                    dailyPrecipitations.add(dailyPrecipitation);
            });
            return dailyPrecipitations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Station> getStationListFromCSV(String pathToFile) {
        List<Station> stations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
                    stations.add(new Station(
                            Long.parseLong(record[0]),
                            record[1],
                            Integer.parseInt(record[2]),
                            Double.parseDouble(record[3]),
                            Double.parseDouble(record[4])
                    ));
            });
            return stations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<RiverPoint> getKocinka(String pathToFile) {
        List<RiverPoint> kocinka = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();
            AtomicReference<Long> lastId = new AtomicReference<>(0L);
            Random random = new Random();
            csvRecords.forEach(record -> {
                kocinka.add(new RiverPoint(
                        lastId.getAndSet(lastId.get() + 1),
                        Double.parseDouble(record[1]),
                        Double.parseDouble(record[0]),
                        random.nextDouble() * 5
                ));
            });
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
        return kocinka;
    }

    public static List<Polygon> getPolygons(String pathToFile) {
        Gson gson = new Gson();
        Polygon[] data = {};
        try (JsonReader reader = new JsonReader(new FileReader(pathToFile))) {
            data = gson.fromJson(reader, Polygon[].class);
            return Arrays.asList(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.asList(data);
    }
}
