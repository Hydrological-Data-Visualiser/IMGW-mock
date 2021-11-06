package pl.edu.agh.imgwmock.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.Station;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ImgwUtils {
    public static List<DailyPrecipitation> getImgwDailyPrecipitationListFromCSV(String pathToFile) {
        List<DailyPrecipitation> dailyPrecipitations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();
            AtomicReference<Long> lastId = new AtomicReference<>(0L);
            csvRecords.forEach(record -> {
                if(Long.parseLong(record[0]) % 6 == 0) {
                    DailyPrecipitation dailyPrecipitation = new DailyPrecipitation(
                            lastId.getAndSet(lastId.get() + 1),
                            Long.parseLong(record[0]),
                            record[1],
                            Instant.parse(record[2] + "-" + record[3] + "-" + record[4] + "T00:00:00Z"),
//                        LocalDate.of(Integer.parseInt(record[2]), Integer.parseInt(record[3]), Integer.parseInt(record[4])),
                            Double.parseDouble(record[5])
                    );
                    dailyPrecipitations.add(dailyPrecipitation);
                }
            });
            return dailyPrecipitations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Station> getIMGWStationListFromCSV(String pathToFile) {
        List<Station> stations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
                if (Long.parseLong(record[0]) % 6 == 0)
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
}
