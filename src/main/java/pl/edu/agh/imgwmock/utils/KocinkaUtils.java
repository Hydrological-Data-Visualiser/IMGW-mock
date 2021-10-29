package pl.edu.agh.imgwmock.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.RiverPoint;
import pl.edu.agh.imgwmock.model.Station;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class KocinkaUtils {
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

    public static List<DailyPrecipitation> getKocinkaTemperatureData() {
        List<DailyPrecipitation> temperature = new ArrayList<>();
        try {
            List<Station> stations = CSVUtils.getStationListFromCSV("src/main/resources/kocinka/kocinka_stations.csv");
            AtomicReference<Long> lastId = new AtomicReference<>(0L);
            for (Station station : stations) {
                String name = station.getName().toLowerCase().replace(" ", "_");
                CSVReader reader = new CSVReader(new FileReader("src/main/resources/kocinka/" + name + ".csv"));
                List<String[]> csvRecords = reader.readAll();
                csvRecords.forEach(record -> {
                    DailyPrecipitation dailyPrecipitation = new DailyPrecipitation(
                            lastId.getAndSet(lastId.get() + 1),
                            station.getId(),
                            station.getName(),
                            LocalDate.parse(record[1], DateTimeFormatter.ofPattern("dd-MM-yyy")),
                            Double.parseDouble(record[4])
                    );
                    temperature.add(dailyPrecipitation);
                });
            }
            return temperature;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<DailyPrecipitation> getKocinkaPressureData() {
        List<DailyPrecipitation> temperature = new ArrayList<>();
        try {
            List<Station> stations = CSVUtils.getStationListFromCSV("src/main/resources/kocinka/kocinka_stations.csv");
            AtomicReference<Long> lastId = new AtomicReference<>(0L);
            for (Station station : stations) {
                String name = station.getName().toLowerCase().replace(" ", "_");
                CSVReader reader = new CSVReader(new FileReader("src/main/resources/kocinka/" + name + ".csv"));
                List<String[]> csvRecords = reader.readAll();
                csvRecords.forEach(record -> {
                    DailyPrecipitation dailyPrecipitation = new DailyPrecipitation(
                            lastId.getAndSet(lastId.get() + 1),
                            station.getId(),
                            station.getName(),
                            LocalDate.parse(record[1], DateTimeFormatter.ofPattern("dd-MM-yyy")),
                            Double.parseDouble(record[6])
                    );
                    temperature.add(dailyPrecipitation);
                });
            }
            return temperature;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

}
