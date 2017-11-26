package airtraffic.stream;

import org.beryx.textio.jline.JLineTextTerminal;

import airtraffic.Airport;
import airtraffic.AirportMetrics;
import airtraffic.Carrier;
import airtraffic.CarrierMetrics;
import airtraffic.Repository;
import airtraffic.TerminalType;

public class StreamingReportsApp extends AbstractReportsApp {
   public static void main(String[] args) throws Exception {
      new StreamingReportsApp().executeSelectedReport();
   }

   @TerminalType(JLineTextTerminal.class)
   public void reportStreamingAirportMetrics(Repository repository) {
      final int year = selectYear();
      final Airport airport = readAirport("Airport");

      clearScreen();
      printf("Airport metrics for %s\n\n", airport.getName());
      println("     Total\t Cancelled\t  Diverted\t   Origins\tDestinations");
      println(repeat("-", 77));

      final AirportMetrics metrics = new AirportMetrics(airport);
      repository.getFlightStream(year)
                .filter(flight -> flight.getOrigin().equals(airport) || 
                                  flight.getDestination().equals(airport))
                .forEach(flight -> {
                   metrics.addFlight(flight);
                   rawPrintf("%,10d\t%,10d\t%,10d\t%,10d\t  %,10d\r", 
                             metrics.getTotalFlights(), 
                             metrics.getTotalCancelled(), 
                             metrics.getTotalDiverted(), 
                             metrics.getTotalOrigins(), 
                             metrics.getTotalDestinations()
                   );
                });

      println();
   }

   @TerminalType(JLineTextTerminal.class)
   public void reportStreamingCarrierMetrics(Repository repository) {
      final int year = selectYear();
      final Carrier carrier = readCarrier();

      clearScreen();
      printf("Carrier metrics for %s\n\n", carrier.getName());
      println("     Total\t Cancelled\t  Diverted\t  Airports");
      println(repeat("-", 59));

      final CarrierMetrics metrics = new CarrierMetrics(carrier);
      repository.getFlightStream(year)
                .filter(flight -> flight.getCarrier().equals(carrier))
                .forEach(flight -> {
                   metrics.addFlight(flight);
                   rawPrintf("%,10d\t%,10d\t%,10d\t%,10d\r",
                             metrics.getTotalFlights(), 
                             metrics.getTotalCancelled(), 
                             metrics.getTotalDiverted(), 
                             metrics.getAirports().size()
                   );
                });

      println();
   }
}