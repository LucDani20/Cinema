package td.cine.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import td.cine.demo.model.Movie;
import td.cine.demo.model.Projection;
import td.cine.demo.model.Reservation;
import td.cine.demo.model.Room;
import td.cine.demo.model.Seat;
import td.cine.demo.model.User;

@Service
public class TicketPdfService {

  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.UTC);

  private static final float MARGIN = 50f;
  private static final float LEADING = 20f;

  public byte[] generate(Reservation reservation) {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A5);
      document.addPage(page);

      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        float y = page.getMediaBox().getHeight() - MARGIN;

        y = writeLine(content, "Billet de cinema", PDType1Font.HELVETICA_BOLD, 18, y);
        y -= LEADING;

        for (String line : buildLines(reservation)) {
          y = writeLine(content, line, PDType1Font.HELVETICA, 11, y);
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.save(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to generate ticket PDF", e);
    }
  }

  private float writeLine(
      PDPageContentStream content, String text, PDType1Font font, float fontSize, float y)
      throws IOException {
    content.beginText();
    content.setFont(font, fontSize);
    content.newLineAtOffset(MARGIN, y);
    content.showText(text);
    content.endText();
    return y - LEADING;
  }

  private List<String> buildLines(Reservation reservation) {
    Projection projection = reservation.getProjection();
    Movie movie = projection.getMovie();
    Room room = projection.getRoom();
    User user = reservation.getUser();

    String seatNumbers =
        reservation.getSeats().stream()
            .map(Seat::getNumber)
            .sorted()
            .collect(Collectors.joining(", "));

    return List.of(
        "Reservation : " + reservation.getId(),
        "Film : " + movie.getTitle(),
        "Genre : " + movie.getGenre(),
        "Salle : " + room.getNumber(),
        "Seance : " + DATETIME_FORMATTER.format(projection.getDatetime()),
        "Sieges : " + (seatNumbers.isBlank() ? "aucun" : seatNumbers),
        "Prix par siege : " + projection.getSeatPrice(),
        "Client : " + user.getFirstName() + " " + user.getLastName(),
        "Reserve le : " + DATETIME_FORMATTER.format(reservation.getCreatedAt()));
  }
}
