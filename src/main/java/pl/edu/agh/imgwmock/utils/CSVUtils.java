package pl.edu.agh.imgwmock.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.Station;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {
    public static List<DailyPrecipitation> getDailyPrecipitationListFromCSV(String pathToFile) {
        List<DailyPrecipitation> dailyPrecipitations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
                // get only stations with id % 11 by now
                if (Long.parseLong(record[0]) % 11 == 0) {
                    DailyPrecipitation dailyPrecipitation = new DailyPrecipitation(
                            Long.parseLong(record[0]),
                            record[1],
                            LocalDate.of(Integer.parseInt(record[2]), Integer.parseInt(record[3]), Integer.parseInt(record[4])),
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

    public static List<Station> getStationListFromCSV(String pathToFile) {
        List<Station> stations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
                // get precipitation from stations with id % 11 by now
                if (Integer.parseInt(record[0]) % 11 == 0) {
                    stations.add(new Station(
                            Long.parseLong(record[0]),
                            record[1],
                            Integer.parseInt(record[2]),
                            Double.parseDouble(record[3]),
                            Double.parseDouble(record[4])
                    ));
                }
            });
            return stations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
