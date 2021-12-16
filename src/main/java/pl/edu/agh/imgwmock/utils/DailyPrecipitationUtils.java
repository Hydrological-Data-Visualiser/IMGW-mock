package pl.edu.agh.imgwmock.utils;

import pl.edu.agh.imgwmock.model.PointData;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class DailyPrecipitationUtils {
    public static Instant getInstantAfterDistinct(List<PointData> list, Instant countFrom, int length){
        List<Instant> countedAfter =
                list.stream()
                        .map(PointData::getDate)
                        .filter(date -> !date.isBefore(countFrom))
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList()); // force sort

        if(countedAfter.size() >= length) return countedAfter.get(length-1);
        else return countedAfter.get(countedAfter.size() - 1);
    }
}
