package com.so.cloudjrb.service;

import com.so.cloudjrb.model.Account;
import com.so.cloudjrb.model.ContaCorrente;
import com.so.cloudjrb.model.Movimentacao;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Serviço dedicado a gerar PDFs de Extrato e Fatura.
 * Isso limpa os Controllers.
 */
@Service
public class ExtratoPdfService {

    private static final Font TITULO_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font INFO_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    /**
     * Gera o extrato da conta e escreve diretamente no OutputStream da resposta HTTP.
     */
    public void gerarExtratoPDF(Account conta, OutputStream outputStream) throws DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        document.add(new Paragraph("Extrato da conta\n\n", TITULO_FONT));
        document.add(new Paragraph("CPF: " + conta.getCpf(), INFO_FONT));
        document.add(new Paragraph("Titular: " + conta.getTitular(), INFO_FONT));
        document.add(new Paragraph("Emitido em: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), INFO_FONT));
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell("Data/Hora");
        table.addCell("Tipo");
        table.addCell("Valor (R$)");

        List<Movimentacao> movs = conta.getMovimentacoes() != null ? conta.getMovimentacoes() : List.of();
        for (Movimentacao m : movs) {
            table.addCell(m.getDataHora());
            table.addCell(m.getTipo());
            table.addCell(String.format(Locale.US, "%.2f", m.getValor()));
        }
        document.add(table);

        document.add(new Paragraph("\nSaldo atual: R$ " + String.format(Locale.US, "%.2f", conta.getSaldo()), INFO_FONT));

        if (conta instanceof ContaCorrente cc) {
            double limite = cc.getLimiteChequeEspecial();
            document.add(new Paragraph("Cheque especial: R$ " + String.format(Locale.US, "%.2f", limite), INFO_FONT));
            document.add(new Paragraph("Saldo disponível: R$ " + String.format(Locale.US, "%.2f", conta.getSaldo() + limite), INFO_FONT));
        }

        document.close();
    }

    // Você pode adicionar 'gerarFaturaPDF' aqui também,
    // copiando a lógica do seu ApiServer.java
}