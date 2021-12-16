package pl.edu.agh.imgwmock.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.Point;
import pl.edu.agh.imgwmock.model.PointData;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ImgwUtils {
    public static List<PointData> getImgwDailyPrecipitationListFromCSV(String pathToFile) {
        List<PointData> dailyPrecipitations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();
            AtomicReference<Long> lastId = new AtomicReference<>(0L);
            csvRecords.forEach(record -> {
//                if (Long.parseLong(record[0]) % 6 == 0) {
                PointData dailyPrecipitation = new PointData(
                        lastId.getAndSet(lastId.get() + 1),
                        Long.parseLong(record[0]),
                        Instant.parse(record[2] + "-" + record[3] + "-" + record[4] + "T00:00:00Z"),
//                        LocalDate.of(Integer.parseInt(record[2]), Integer.parseInt(record[3]), Integer.parseInt(record[4])),
                        Double.parseDouble(record[5])
                );
                dailyPrecipitations.add(dailyPrecipitation);
//                }
            });
            return dailyPrecipitations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Point> getIMGWStationListFromCSV(String pathToFile) {
        List<Point> stations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
//                if (Long.parseLong(record[0]) % 6 == 0)
                stations.add(new Point(
                        Long.parseLong(record[0]),
                        record[1],
                        new Double[]{Double.parseDouble(record[3]), Double.parseDouble(record[4])}
                ));
            });
            return stations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Point> getStationsWhereAllDataAreNotNull() {
        List<Point> stations = getIMGWStationListFromCSV("src/main/resources/wykaz_stacji.csv");
        List<PointData> dailyPrecipitations = getImgwDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        dailyPrecipitations = new ArrayList<>(dailyPrecipitations);
        stations = new ArrayList<>(stations);
        Map<Long, ArrayList<PointData>> stationsMap = new HashMap<>();
        for (PointData precipitation : dailyPrecipitations) {
            if (stationsMap.containsKey(precipitation.getStationId())) {
                ArrayList<PointData> list = stationsMap.get(precipitation.getStationId());
                list.add(precipitation);
                stationsMap.remove(precipitation.getStationId());
                stationsMap.put(precipitation.getStationId(), list);
            } else {
                stationsMap.put(precipitation.getStationId(), new ArrayList<>() {{
                    add(precipitation);
                }});
            }
        }
        List<Point> stationsFromId = new ArrayList<>();
        for (Map.Entry<Long, ArrayList<PointData>> entry : stationsMap.entrySet()) {
            if (entry.getValue().stream().anyMatch(n -> n.getValue() != null)) {
                Optional<Point> station = stations.stream().filter(d -> d.getId().equals(entry.getKey())).findFirst();
                station.ifPresent(stationsFromId::add);
            }
        }
        return stationsFromId;
    }


    public static List<PointData> getDailyPrecipitationsFromStationsWhereAllDataAreNotNull() {
        List<Point> stations = getStationsWhereAllDataAreNotNull();
        List<PointData> dailyPrecipitations = getImgwDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        List<Long> stationsId = stations.stream().map(Point::getId).collect(Collectors.toList());
        dailyPrecipitations = dailyPrecipitations.stream().filter(rain -> stationsId.contains(rain.getStationId())).collect(Collectors.toList());
        return dailyPrecipitations;
    }
}
