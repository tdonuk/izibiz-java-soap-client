package com.edonusum.client.sample.eiarchive;

import com.edonusum.client.adapter.AuthAdapter;
import com.edonusum.client.adapter.EiarchiveAdapter;
import com.edonusum.client.adapter.EinvoiceAdapter;
import com.edonusum.client.sample.auth.AuthTests;
import com.edonusum.client.util.XMLUtils;
import com.edonusum.client.wsdl.eiarchive.*;
import com.edonusum.client.wsdl.einvoice.SendInvoiceRequest;
import com.edonusum.client.wsdl.einvoice.SendInvoiceResponse;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
public class EiarchiveTests {
    private final AuthAdapter authAdapter = new AuthAdapter();
    private final EiarchiveAdapter eiarchiveAdapter = new EiarchiveAdapter();

    private String getSessionId() {
        String sessionId = authAdapter.login(AuthTests.prepareLoginRequest()).getSESSIONID();
        return sessionId;
    }

    @Test
    public void givenInvoiceId_then_returnsArchiveInvoice() { // readFromArchive
        ArchiveInvoiceReadRequest request = new ArchiveInvoiceReadRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());

        request.setINVOICEID("08f7d8cf-ff85-40d2-8fe8-14028e865108");
        request.setPORTALDIRECTION("OUT");
        request.setPROFILE("XML");

        request.setREQUESTHEADER(header);

        ArchiveInvoiceReadResponse resp = eiarchiveAdapter.readFromArchive(request);

        Assertions.assertNull(resp.getERRORTYPE());

        System.out.println(resp.getINVOICE().get(0));
    }

    @Test
    public void givenInvoiceId_then_returnsEArchiveInvoiceStatus() { // getEArchiveStatus
        GetEArchiveInvoiceStatusRequest request = new GetEArchiveInvoiceStatusRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());
        request.setREQUESTHEADER(header);

        request.getUUID().add("08f7d8cf-ff85-40d2-8fe8-14028e865108");

        GetEArchiveInvoiceStatusResponse resp = eiarchiveAdapter.getEArchiveStatus(request);

        Assertions.assertNull(resp.getERRORTYPE());

        System.out.println(resp.getINVOICE().get(0).getHEADER().getSTATUSDESC());
    }

    @Test
    public void givenInvoiceUUID_then_cancelsInvoice() { // cancelEArchiveInvoiceResponse
        CancelEArchiveInvoiceRequest request = new CancelEArchiveInvoiceRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());

        request.setREQUESTHEADER(header);

        CancelEArchiveInvoiceRequest.CancelEArsivInvoiceContent content = new CancelEArchiveInvoiceRequest.CancelEArsivInvoiceContent();
        content.setFATURAUUID("08f7d8cf-ff85-40d2-8fe8-14028e865108");

        request.getCancelEArsivInvoiceContent().add(content);

        CancelEArchiveInvoiceResponse resp = eiarchiveAdapter.cancelEArchiveInvoice(request);

        Assertions.assertNull(resp.getERRORTYPE());

        System.out.println(resp.getREQUESTRETURN().getINTLTXNID());
    }

    @Test
    public void givenInvoiceUUID_withDeleteFlag_then_cancelsInvoice() { // cancelEArchiveInvoiceResponse
        CancelEArchiveInvoiceRequest request = new CancelEArchiveInvoiceRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());

        request.setREQUESTHEADER(header);

        CancelEArchiveInvoiceRequest.CancelEArsivInvoiceContent content = new CancelEArchiveInvoiceRequest.CancelEArsivInvoiceContent();
        content.setFATURAUUID("08f7d8cf-ff85-40d2-8fe8-14028e865108");
        /* GiB'e hiçbir zaman raporlanmamalı ise: */
        content.setDELETEFLAG("Y");

        /* GiB'e iptal olarak raporlanmalı ise:
        * content.setDELETEFLAG("N");
        */

        /*
        * E_Arşiv platformunda bulunmayan bir faturanın iptali için:
        * content.setUPLOADFLAG("Y")
        * */

        request.getCancelEArsivInvoiceContent().add(content);

        CancelEArchiveInvoiceResponse resp = eiarchiveAdapter.cancelEArchiveInvoice(request);

        Assertions.assertNull(resp.getERRORTYPE());

        System.out.println(resp.getREQUESTRETURN().getINTLTXNID());
    }

    @Test
    public void givenInvoiceUUID_andValidEmail_then_sendsInvoiceToEmail() { // getEmailEarchiveInvoice
        GetEmailEarchiveInvoiceRequest request = new GetEmailEarchiveInvoiceRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());
        request.setREQUESTHEADER(header);

        request.setFATURAUUID("08f7d8cf-ff85-40d2-8fe8-14028e865108");
        request.setEMAIL("example@email.com"); // test edilmek istenen e-posta adresi

        GetEmailEarchiveInvoiceResponse resp = eiarchiveAdapter.getEmailEarchiveInvoice(request);

        Assertions.assertNull(resp.getERRORTYPE());

        System.out.println(resp.getREQUESTRETURN().getRETURNCODE());
    }

    @Test
    public void givenPeriod_andFlag_then_returnsReportList() { // getEarchiveReport
        GetEArchiveReportRequest request = new GetEArchiveReportRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());
        request.setREQUESTHEADER(header);

        DecimalFormat format = new DecimalFormat("#00");

        // period text
        String period = LocalDate.now().getYear() + format.format(LocalDate.now().getMonthValue());  // 2018 mayıs = 201805 şeklinde formatlanmalı

        request.setREPORTPERIOD(period);

        request.setREPORTSTATUSFLAG("Y");

        GetEArchiveReportResponse resp = eiarchiveAdapter.getEarchiveReport(request);

        Assertions.assertNull(resp.getERRORTYPE());

        System.out.println(resp.getREPORT().get(0).getREPORTSTATUS());
    }

    // Rapor listesi ile yapılan testlerde kullanılmak üzere yazılmıştır, getEarchiveReport ile aynı işi yapmaktadır
    private List<REPORT> getReportList(String sessionId) {
        GetEArchiveReportRequest request = new GetEArchiveReportRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(sessionId);
        request.setREQUESTHEADER(header);

        DecimalFormat format = new DecimalFormat("#00");

        // period text
        String period = LocalDate.now().getYear() + format.format(LocalDate.now().getMonthValue());  // 2018 mayıs = 201805 şeklinde formatlanmalı

        request.setREPORTPERIOD(period);

        request.setREPORTSTATUSFLAG("Y");

        GetEArchiveReportResponse resp = eiarchiveAdapter.getEarchiveReport(request);

        return resp.getREPORT();
    }

    @Test
    public void givenReportId_thenReturnsReportContent() throws IOException {
        ReadEArchiveReportRequest request = new ReadEArchiveReportRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());

        request.setREQUESTHEADER(header);

        List<REPORT> reportList = getReportList(header.getSESSIONID());

        ReadEArchiveReportResponse response;

        for (REPORT rep : reportList) {
            request.setRAPORNO(rep.getREPORTNO());
            response = eiarchiveAdapter.readEarchiveReport(request);

            Assertions.assertNull(response.getERRORTYPE());

            System.out.println(response.getREQUESTRETURN().getRETURNCODE());
        }
    }

    @Test
    public void givenValidEiarchive_then_writesToArchive() throws IOException { // writeToEiArchiveExtended
        ArchiveInvoiceExtendedRequest request = new ArchiveInvoiceExtendedRequest();
        REQUESTHEADERType header = new REQUESTHEADERType();

        header.setSESSIONID(getSessionId());
        header.setCOMPRESSED("N");
        request.setREQUESTHEADER(header);

        ArchiveInvoiceExtendedContent content = new ArchiveInvoiceExtendedContent();
        ArchiveInvoiceExtendedContent.INVOICEPROPERTIES props = new ArchiveInvoiceExtendedContent.INVOICEPROPERTIES();

        props.setEARSIVFLAG(FLAGVALUE.Y);

        //invoice ID example: 'ABC2009123456789'
        DecimalFormat formatter = new DecimalFormat("#000000000"); // her zaman 9 haneli olmalı
        Random random = new Random();
        long id = random.nextInt(999999999); // 9 haneli
        String invoiceId = "X01" + LocalDate.now().getYear() + formatter.format(id); // seri + yıl + 9 haneli id
        UUID invoiceUUID = UUID.randomUUID();

        File draftFile = new File("xml\\draft-eiarchive.xml");
        File createdXml = XMLUtils.createXmlFromDraftInvoice(draftFile, invoiceUUID, invoiceId);

        Base64Binary b64 = new Base64Binary();
        b64.setValue(Files.readAllBytes(createdXml.toPath()));

        props.setINVOICECONTENT(b64);

        EARSIVPROPERTIES eiarchiveProps = new EARSIVPROPERTIES();
        eiarchiveProps.setEARSIVTYPE(EARSIVTYPEVALUE.NORMAL);
        eiarchiveProps.setSUBSTATUS(SUBSTATUSVALUE.DRAFT); // new or draft

        props.setEARSIVPROPERTIES(eiarchiveProps);

        content.getINVOICEPROPERTIES().add(props);

        request.setArchiveInvoiceExtendedContent(content);

        ArchiveInvoiceExtendedResponse resp = eiarchiveAdapter.writeToArchiveExtended(request);

        createdXml.delete();

        Assertions.assertNull(resp.getERRORTYPE());

        System.out.println(resp.getREQUESTRETURN().getRETURNCODE());
    }

}
