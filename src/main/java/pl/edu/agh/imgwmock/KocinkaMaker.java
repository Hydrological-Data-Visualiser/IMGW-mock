package pl.edu.agh.imgwmock;

import lombok.val;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.utils.NewKocinkaUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class KocinkaMaker {
    public static void main(String[] args) throws IOException {
        Instant instant = Instant.now();
        val availableDates = new ArrayList<>(List.of(instant));
        for (int i = 0; i < 40; i++) {
            availableDates.add(instant.minusSeconds(60 * 60 * 12));
            instant = instant.minusSeconds(60 * 60 * 12);
        }
        Long lastId = 0L;
        val availableDatesSorted = availableDates.stream().sorted().collect(Collectors.toList());
        Random random = new Random();
        val kocinka = NewKocinkaUtils.getKocinkaStations();


        FileWriter fileWriter = new FileWriter("kocinkaRandom.csv");
        PrintWriter printWriter = new PrintWriter(fileWriter);


        for (Instant inst : availableDatesSorted) {
            for (Station station : kocinka) {
                System.out.println(lastId + "," + station.getId() + "," + String.valueOf(random.nextDouble() * 10 ).substring(0, 5) + "," + inst);
                printWriter.println(lastId + "," + station.getId() + "," + String.valueOf(random.nextDouble() * 10 ).substring(0, 5)+ "," + inst);
                lastId += 1;
            }
        }


    }


}
