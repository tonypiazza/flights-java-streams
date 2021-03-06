package airtraffic.reports.iterator;

import java.util.Iterator;

import org.beryx.textio.TextTerminal;

import airtraffic.Airport;
import airtraffic.Carrier;
import airtraffic.Flight;
import airtraffic.ReportContext;
import airtraffic.annotations.IteratorStyle;
import airtraffic.metrics.AirportMetrics;
import airtraffic.metrics.CarrierMetrics;
import airtraffic.reports.LiveReports;


/**
 * Implementation of live reports using iterator style that was common in
 * Java 7 and earlier versions.
 *
 * @author Tony Piazza <tony@piazzaconsulting.com>
 */
@IteratorStyle
public class IteratorLiveReports implements LiveReports {

   @Override
   public void reportAirportMetrics(ReportContext context) {
      final int year = context.getYear();
      final Airport airport = context.getAirport();

      TextTerminal<?> terminal = context.getTerminal();
      final AirportMetrics metrics = new AirportMetrics(airport);
      Iterator<Flight> iterator = context.getRepository()
                                         .getFlightIterator(year);
      while(iterator.hasNext()) {
         Flight flight = iterator.next();
         if(flight.getOrigin().equals(airport) || 
            flight.getDestination().equals(airport)) {
            metrics.addFlight(flight);
            terminal.printf("%,10d\t%,10d\t%,10d\t%,10d\t  %,10d", 
                            metrics.getTotalFlights(), 
                            metrics.getTotalCancelled(), 
                            metrics.getTotalDiverted(), 
                            metrics.getTotalOrigins(), 
                            metrics.getTotalDestinations());
            terminal.moveToLineStart();
         }
      }

      terminal.println();
   }

   public void reportCarrierMetrics(ReportContext context) {
      final int year = context.getYear();
      final Carrier carrier = context.getCarrier();

      TextTerminal<?> terminal = context.getTerminal();
      final CarrierMetrics metrics = new CarrierMetrics(carrier);
      Iterator<Flight> iterator = context.getRepository()
                                         .getFlightIterator(year);
      while(iterator.hasNext()) {
         Flight flight = iterator.next();
         if(flight.getCarrier().equals(carrier)) {
            metrics.addFlight(flight);
            terminal.printf("%,10d\t%,10d\t%,10d\t%,10d", 
                            metrics.getTotalFlights(), 
                            metrics.getTotalCancelled(), 
                            metrics.getTotalDiverted(), 
                            metrics.getAirports().size());
            terminal.moveToLineStart();
         }
      }

      terminal.println();
   }
}