package pl.edu.agh.imgwmock.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.PolylineDataNew;
import pl.edu.agh.imgwmock.model.Station;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class NewKocinkaUtils {

    public static List<Station> getKocinkaStations() {
        List<Station> kocinka = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/kocinka.csv"))) {
            List<String[]> csvRecords = reader.readAll();
            AtomicReference<Long> lastId = new AtomicReference<>(0L);
            for (int i = 0; i < csvRecords.size() - 1; i++) {
                Double[] firstPoint = new Double[2];
                Double[] secondPoint = new Double[2];
                firstPoint[0] = Double.parseDouble(csvRecords.get(i)[1]);
                firstPoint[1] = Double.parseDouble(csvRecords.get(i)[0]);
                secondPoint[0] = Double.parseDouble(csvRecords.get(i + 1)[1]);
                secondPoint[1] = Double.parseDouble(csvRecords.get(i + 1)[0]);

                kocinka.add(new Station(
                        lastId.getAndSet(lastId.get() + 1),
                        "",
                        List.of(firstPoint, secondPoint)
                ));
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
        return kocinka;
    }

    public static List<PolylineDataNew> getNewKocinkaRandomData(Optional<String> instant) {
        List<Station> stations = getKocinkaStations();
        List<PolylineDataNew> result = new ArrayList<>();
        Random random = new Random();
        Long lastId = 0L;
        for (Station station : stations) {
            result.add(new PolylineDataNew(
                    lastId,
                    station.getId(),
                    random.nextDouble() * 10,
                    Instant.parse(instant.orElse("2011-12-03T10:15:30Z"))
            ));
            lastId += 1;
        }
        return result;
    }

    public static List<PolylineDataNew> getNewKocinkaRandomDataNewNew() {
        List<PolylineDataNew> kocinka = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/kocinkaRandom.csv"))) {
            List<String[]> csvRecords = reader.readAll();
            for (int i = 0; i < csvRecords.size() - 1; i++) {
                kocinka.add(new PolylineDataNew(
                        Long.parseLong(csvRecords.get(i)[0]),
                        Long.parseLong(csvRecords.get(i)[1]),
                        Double.parseDouble(csvRecords.get(i)[2]),
                        Instant.parse(csvRecords.get(i)[3])
                ));
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
        return kocinka;
    }


}
