package cc.raupach.sync.google;


import cc.raupach.sync.config.ShopwareSyncProperties;
import cc.raupach.sync.google.dto.GoogleFeedDto;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;


@Service
@Slf4j
public class GoogleService {
  private static final String APPLICATION_NAME = "Shopware Google Feed Sync";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  private Sheets sheetsService;

  @Autowired
  private ShopwareSyncProperties shopwareSyncProperties;

  @PostConstruct
  public void setup() throws GeneralSecurityException, IOException {
    NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
      .setApplicationName(APPLICATION_NAME)
      .build();
  }


  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    // Load client secrets.
    InputStream in = GoogleService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
      .setAccessType("offline")
      .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }


  public void writeSheet(String name, Collection<GoogleFeedDto> products) throws IOException, URISyntaxException {

    String spreadSheetId = shopwareSyncProperties.getTagsForFeeds().get(name);

    clearValues(spreadSheetId);
    writeHeader(spreadSheetId);

    Iterator<GoogleFeedDto> it = products.iterator();
    int row = 2;
    while (it.hasNext()) {
      writeProduct(row++, it.next(), spreadSheetId);
    }
  }

  private void writeProduct(int r, GoogleFeedDto product, String spreadSheetId) throws IOException, URISyntaxException {

    URI uri = new URI(product.getMediaLink());
    String landingPage = uri.getScheme() + "://" + uri.getHost() + "/" + product.getUrl();

    ValueRange body = new ValueRange().setValues(List.of(
      Arrays.asList(
        product.getProductNumber() == null ? "" : product.getProductNumber(),
        product.getName() == null ? "" : product.getName(),
        product.getDescription() == null ? "" : product.getDescription(),
        landingPage,
        "new",
        product.getPrice() + " " + product.getCurrency(),
        "in_stock",
        product.getMediaLink() == null ? "" : product.getMediaLink(),
        "",
        "",
        "When too perfect lieber Gott böse",
        "",
        "AT:::3.49 EUR,DE:::3.49 EUR",
        "Nein")));

    sheetsService.spreadsheets().values()
      .update(spreadSheetId, "A" + r, body)
      .setValueInputOption("RAW")
      .execute();
  }

  private void clearValues(String spreadSheetId) throws IOException {
    ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
    sheetsService.spreadsheets().values().clear(spreadSheetId, "Sheet1", clearValuesRequest);
  }

  private void writeHeader(String spreadSheetId) throws IOException {
    ValueRange body = new ValueRange().setValues(List.of(
      Arrays.asList("ID", "Titel", "Beschreibung", "Produkt Url", "Zustand", "Preis", "Verfügbarkeit", "Bild Link", "gtin", "mpn", "Marke", "Google Produktkategorie", "Versand", "Kennzeichnung existiert")));

    sheetsService.spreadsheets().values()
      .update(spreadSheetId, "A1", body)
      .setValueInputOption("RAW")
      .execute();
  }

}
