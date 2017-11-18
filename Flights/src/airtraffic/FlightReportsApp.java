package airtraffic;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Generate various flight statistics using Java 8 streams.
 * 
 * @author tony@piazzaconsulting.com
 */
public class FlightReportsApp extends AbstractReportsApp {
	private static final List<DistanceRange> DISTANCE_RANGES =
		Arrays.asList(DistanceRange.between(0, 100), 
					  DistanceRange.between(101, 250),
					  DistanceRange.between(251, 500),
					  DistanceRange.between(501, 1000),
					  DistanceRange.between(1001, 2500),
					  DistanceRange.between(2501, 5000));

	public static void main(String[] args) throws Exception {
		ReferenceData reference = new ReferenceData();
		Stream<Flight> source = Files.lines(Paths.get("data/flights-2008.csv"))
									 .skip(1)	// skip header
									 .map(s -> new Flight(s, reference));
		FlightReportsApp app = new FlightReportsApp();
		app.executeSelectedReport(source);
	}

	public void reportTotalFlightsFromOrigin(Stream<Flight> source) {
		String origin = readString("Origin");
		long count = source.filter(f -> f.notCancelled() && 
				                        f.getOrigin().getIATA().equals(origin))
						   .count();
		printf("Total flights from %s is %,d\n", origin, count);
	}

	public void reportTotalFlightsToDestination(Stream<Flight> source) {
		String destination = readString("Destination");
		long count = source.filter(f -> f.notCancelled() && 
				                        f.getDestination().getIATA().equals(destination))
						   .count();
		printf("Total flights to %s is %,d\n", destination, count);
	}

	public void reportTotalFlightsFromOriginToDestination(Stream<Flight> source) {
		String origin = readString("Origin");
		String destination = readString("Destination");
		long count = source.filter(f -> f.notCancelled() && 
				                        f.getOrigin().getIATA().equals(origin) &&
				                        f.getDestination().getIATA().equals(destination))
						   .count();
		printf("Total flights from %s to %s is %,d\n", origin, destination, count);
	}

	public void reportMostFlightsByOrigin(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("\nOrigin\tCount");
		println("---------------");
		source.filter(f -> f.notCancelled())
			  .collect(groupingBy(Flight::getOrigin, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf("%3s\t%d\n", e.getKey().getIATA(), e.getValue()));
	}

	public void reportTopDestinationsFromOrigin(Stream<Flight> source) {
		String origin = readString("Origin");
		int limit = readLimit(10, 1, 100);
		println("\nDestination\t   Count");
		println("------------------------------");
		source.filter(f -> f.notCancelled() && f.getOrigin().getIATA().equals(origin))
			  .collect(groupingBy(Flight::getDestination, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf(" %3s\t\t%,10d\n", e.getKey().getIATA(), e.getValue()));
	}

	public void reportMostPopularRoutes(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("Route\t\t    Count");
		println("---------------------------");
		source.filter(f -> f.notCancelled())
			  .collect(groupingBy(Flight::getRoute, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf("%s\t%,10d\n", e.getKey(), e.getValue().intValue()));
	}

	public void reportWorstAverageDepartureDelayByOrigin(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("Origin\tDelay (min)");
		println("----------------------");
		source.filter(f -> f.notCancelled())
			  .collect(groupingBy(Flight::getOrigin, averagingInt(f -> f.getDepartureDelay())))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf(" %3s\t\t%.0f\n", e.getKey().getIATA(), e.getValue()));
	}

	public void reportWorstAverageArrivalDelayByDestination(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("Destination\tDelay (min)");
		println("----------------------------");
		source.filter(f -> f.notCancelled())
			  .collect(groupingBy(Flight::getDestination, averagingInt(f -> f.getArrivalDelay())))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf(" %3s\t\t\t%.0f\n", e.getKey().getIATA(), e.getValue()));
	}

	public void reportMostCancelledFlightsByOrigin(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("Origin\t\t  Count");
		println("---------------------------");
		source.filter(f -> f.cancelled())
			  .collect(groupingBy(Flight::getOrigin, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf(" %3s\t\t%,8d\n", e.getKey().getIATA(), e.getValue()));
	}

	public void reportMostCancelledFlightsByCarrier(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("Carrier\t\t\t\t Count");
		println("-----------------------------------------");
		source.filter(f -> f.cancelled())
		      .map(f -> f.getCarrier())
			  .collect(groupingBy(Carrier::getName, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf("%-24s\t%,8d\n", left(e.getKey(), 24), e.getValue()));
	}

	public void reportCarrierMetrics(Stream<Flight> source) {
		print("Code    Carrier Name                        ");
		println("Total        Cancelled %   Diverted %    Airports");
		println(repeat("-", 94));
		source.collect(HashMap::new, CarrierMetrics.accumulator(), CarrierMetrics.combiner())
			  .entrySet()
			  .stream()
			  .sorted(comparingByKey())
			  .forEach(e -> {
				  CarrierMetrics metrics = e.getValue();
				  Carrier carrier = metrics.getSubject();
				  String carrierName = carrier.getName();
				  printf(" %2s     %-30s     %,9d    %6.1f        %6.1f         %,5d\n", 
				  		  carrier.getCode(),
				  		  carrierName.substring(0, Math.min(carrierName.length(), 29)),
				  		  metrics.getTotalFlights(),
				  		  metrics.getTotalCancelled() * 100.0 / metrics.getTotalFlights(),
				  		  metrics.getTotalDiverted() * 100.0 / metrics.getTotalFlights(),
				  		  metrics.getAirports().size()
				  );
			  });
	}

	public void reportFlightCountsByAircraftType(Stream<Flight> source) {
		println("Aircraft Type\t\t\tCount");
		println("-------------------------------------------");
		source.filter(f -> f.notCancelled())
			  .map(f -> f.getPlane())
			  .collect(groupingBy(Plane::getAircraftType, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .forEachOrdered(e -> printf("%-25s\t%,10d\n", e.getKey(), e.getValue()));
	}

	public void reportFlightCountsByEngineType(Stream<Flight> source) {
		println("Engine Type\t\t\tCount");
		println("-------------------------------------------");
		source.filter(f -> f.notCancelled())
			  .map(f -> f.getPlane())
			  .collect(groupingBy(Plane::getEngineType, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .forEachOrdered(e -> printf("%-25s\t%,10d\n", e.getKey(), e.getValue()));
	}

	public void reportFlightCountsByManufacturer(Stream<Flight> source) {
		println("Manufacturer\t\t\t Count");
		println("-------------------------------------------");
		source.filter(f -> f.notCancelled())
			  .map(f -> f.getPlane())
			  .collect(groupingBy(Plane::getManufacturer, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .forEachOrdered(e -> printf("%-25s\t%,10d\n", e.getKey(), e.getValue()));
	}

	public void reportFlightCountsByPlaneYear(Stream<Flight> source) {
		println("Year\t  Count");
		println("-------------------");
		source.filter(f -> f.notCancelled())
			  .map(f -> f.getPlane())
			  .collect(groupingBy(Plane::getYear, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .forEachOrdered(e -> printf("%4s\t%,10d\n", 
					  						e.getKey().longValue() == 0 ? "????" : e.getKey(), 
					  						e.getValue()));
	}

	public void reportMostFlightsByOriginState(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("State\t  Count");
		println("-------------------");
		source.filter(f -> f.notCancelled())
		      .map(f -> f.getOrigin())
			  .collect(groupingBy(Airport::getState, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf("%2s\t%,10d\n", e.getKey(), e.getValue()));
	}

	public void reportMostFlightsByDestinationState(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("State\tCount");
		println("-------------------");
		source.filter(f -> f.notCancelled())
		      .map(f -> f.getDestination())
			  .collect(groupingBy(Airport::getState, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> printf("%2s\t%,10d\n", e.getKey(), e.getValue()));
	}

	public void reportMostFlightsByPlane(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("Tail #\t  Manufacturer\t\tModel #\t\tCount\t Daily Avg");
		println(repeat("-", 67));
		source.filter(f -> f.notCancelled())
			  .collect(groupingBy(Flight::getPlane, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .forEachOrdered(e -> {
				  Plane plane = e.getKey();
				  printf("%-8s  %-20s  %-10s  %,10d\t  %6.1f\n", 
						  plane.getTailNumber(), 
						  left(plane.getManufacturer(), 20),
						  left(plane.getModel().getModelNumber(), 10),
						  e.getValue(),
						  e.getValue().floatValue() / 365);  
			  });
	}

	public void reportMostFlightsByPlaneModel(Stream<Flight> source) {
		int limit = readLimit(10, 1, 100);
		println("Manufacturer\t\t\tModel #\t\t\t  Count\t\tDaily Avg");
		println(repeat("-", 82));
		source.filter(f -> f.notCancelled())
			  .map(p -> p.getPlane())
			  .collect(groupingBy(Plane::getModel, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByValue(reverseOrder()))
			  .limit(limit)
			  .map(e -> {
				  	PlaneModel model = e.getKey();
				  	Long count = e.getValue();
			  		return String.format("%-25s\t%-20s\t%,10d\t%8.1f",
			  							 model.getManufacturer(),
			  							 model.getModelNumber(),
			  							 count,
			  							 count.floatValue() / 365);
			  }).forEachOrdered(s -> println(s));
	}

	public void reportLongestFlights(Stream<Flight> source) {
		byDistance(source, comparingInt(Flight::getDistance).reversed());
	}

	public void reportShortestFlights(Stream<Flight> source) {
		byDistance(source, comparingInt(f -> f.getDistance()));
	}

	private void byDistance(Stream<Flight> source, Comparator<Flight> comparator) {
		int limit = readLimit(10, 1, 100);
		println("Flight #     Date\tOrigin\tDestination\tDistance");
		println(repeat("-", 57));
		source.filter(f -> f.notCancelled())
			  .sorted(comparator)
			  .limit(limit)
			  .map(flight -> String.format("%-8s  %10s\t %3s\t    %3s\t\t%4d",
											flight.getFlightNumber(),
											formatDate(flight.getDate()),
											flight.getOrigin().getIATA(),
  											flight.getDestination().getIATA(),
					  						flight.getDistance()))
			  .forEachOrdered(s -> println(s));
	}

	public void reportFlightsByDistanceRange(Stream<Flight> source) {
		println("Range\t\tCount");
		println(repeat("-", 27));
		source.filter(f -> f.notCancelled())
			  .collect(groupingBy(rangeClassifier(), counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparingByKey())
			  .forEach(e -> printf("%-10s\t%,10d\n", e.getKey(), e.getValue()));
	}

	private static Function<Flight, DistanceRange> rangeClassifier() {
		return f -> DISTANCE_RANGES.stream()
								   .filter(r -> r.contains(f.getDistance()))
								   .findAny()
								   .get();
	}

	public void reportDaysWithLeastCancellations(Stream<Flight> source) {
		byDaysWithCancellations(source, comparingByValue());
	}

	public void reportDaysWithMostCancellations(Stream<Flight> source) {
		byDaysWithCancellations(source, comparingByValue(reverseOrder()));
	}

	private void byDaysWithCancellations(Stream<Flight> source, 
		Comparator<Entry<Date, Long>> comparator) {
		int limit = readLimit(10, 1, 100);
		println("Date\t\tCount");
		println(repeat("-", 24));
		source.filter(f -> f.cancelled())
			  .collect(groupingBy(Flight::getDate, counting()))
			  .entrySet()
			  .stream()
			  .sorted(comparator)
			  .limit(limit)
			  .forEach(e -> printf("%-10s       %,3d\n", 
					  				formatDate(e.getKey()), 
					  				e.getValue()));
	}

	public void reportAirportMetrics(Stream<Flight> source) {
		print("IATA    Airport Name                        ");
		println("Total        Cancelled %   Diverted %");
		println(repeat("-", 82));
		source.collect(HashMap::new, AirportMetrics.accumulator(), AirportMetrics.combiner())
		  .entrySet()
		  .stream()
		  .sorted(comparingByKey())
		  .forEach(e -> {
			  AirportMetrics metrics = e.getValue();
			  Airport airport = metrics.getSubject();
			  String airportName = airport.getName();
			  printf(" %3s    %-30s     %,9d    %6.1f        %6.1f\n", 
			  		  airport.getIATA(),
			  		  airportName.substring(0, Math.min(airportName.length(), 29)),
			  		  metrics.getTotalFlights(),
			  		  metrics.getTotalCancelled() * 100.0 / metrics.getTotalFlights(),
			  		  metrics.getTotalDiverted() * 100.0 / metrics.getTotalFlights()
			  );
		  });
	}
}