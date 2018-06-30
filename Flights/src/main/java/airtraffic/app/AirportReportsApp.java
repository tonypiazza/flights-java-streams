package airtraffic.app;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.apache.commons.lang3.StringUtils.left;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.beryx.textio.TextTerminal;
import airtraffic.ReportContext;
import airtraffic.reports.AirportReports;
import airtraffic.reports.ReportException;
import airtraffic.reports.iterator.IteratorAirportReports;
import airtraffic.reports.stream.StreamAirportReports;

public class AirportReportsApp extends AbstractReportsApp implements AirportReports {
    public static void main(String[] args) throws Exception {
        new AirportReportsApp().executeSelectedReport();
    }

    @Override
    public ResultSet reportAirportMetrics(ReportContext context) {
        final String style = readStyleOption();
        context.setYear(readYear());

        TextTerminal<?> terminal = context.getTerminal();
        terminal.print("\nIATA    Airport Name                        ");
        terminal.println("Total        Cancelled %   Diverted %");
        terminal.println(repeat("-", 82));

        ResultSet rs = getImpl(style).reportAirportMetrics(context);
        try {
           while (rs.next()) {
              terminal.printf("%3s     %-30s     %,9d    %6.1f        %6.1f\n", 
                              rs.getString("IATA"),
                              left(rs.getString("Name"), 29),
                              rs.getInt("TotalFlights"),
                              rs.getDouble("CancellationRate") * 100.0,
                              rs.getDouble("DiversionRate") * 100.0);
           }
        } catch (SQLException e) {
           throw new ReportException(e);
        }
        return rs;
    }

    @Override
    public ResultSet reportAirportsForState(ReportContext context) throws ReportException {
        final String style = readStyleOption();
        context.setState(readState());

        TextTerminal<?> terminal = context.getTerminal();
        terminal.println("\nIATA\tAirport Name\t\t\t\t\tCity");
        terminal.println(repeat("-", 77));

        ResultSet rs = getImpl(style).reportAirportsForState(context);
        try {
            while(rs.next()) {
                terminal.printf("%3s\t%-40s\t%-20s\n",
                                rs.getString("IATA"),
                                rs.getString("Name"),
                                rs.getString("City"));
            }
        } catch (SQLException e) {
            throw new ReportException(e);
        }
        return rs;
    }

    @Override
    public ResultSet reportAirportsNearLocation(ReportContext context) throws ReportException {
        final String style = readStyleOption();
        context.setLocation(readGeoLocation()).setDistance(readDistanceInMiles());

        TextTerminal<?> terminal = context.getTerminal();
        terminal.println("\nIATA\tAirport Name\t\t\t\t\tState\tCity\t\tDistance");
        terminal.println(repeat("-", 89));

        ResultSet rs = getImpl(style).reportAirportsNearLocation(context);
        try {
           while(rs.next()) {
              terminal.printf("%3s\t%-40s\t %2s\t%-15s    %,4.0f\n",
                              rs.getString("IATA"), 
                              rs.getString("Name"), 
                              rs.getString("State"), 
                              left(rs.getString("City"), 15),
                              rs.getString("Distance"));
           }
        } catch (SQLException e) {
           throw new ReportException(e);
        }
        return rs;
    }

    @Override
    public ResultSet reportAirportsWithHighestCancellationRate(ReportContext context) throws ReportException {
        final String style = readStyleOption();
        context.setYear(readYear()).setLimit(readLimit(10, 1, 100));

        TextTerminal<?> terminal = context.getTerminal();
        terminal.println("\nIATA\tName\t\t\t\tRate");
        terminal.println(repeat("-", 47));

        ResultSet rs = getImpl(style).reportAirportsWithHighestCancellationRate(context);
        try {
           while(rs.next()) {
              terminal.printf("%3s\t%-30s\t%6.1f\n",
                              rs.getString("IATA"),
                              rs.getString("Name"),
                              rs.getDouble("CancellationRate") * 100);
           }
        } catch (SQLException e) {
           throw new ReportException(e);
        }
        return rs;
    }

    private AirportReports getImpl(String style) {
        return "iterator".equals(style) ? new IteratorAirportReports() : new StreamAirportReports();
    }
}