package com.anonchat.backend.service;

import com.anonchat.backend.model.ChatMessage;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public ByteArrayInputStream exportChatToPdf(String roomId, List<Object> history) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Chat History: " + roomId, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Fonts
            Font senderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font messageFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font sysFont = FontFactory.getFont(FontFactory.COURIER_OBLIQUE, 10, java.awt.Color.GRAY);

            for (Object item : history) {
                // Check if the item is a ChatMessage Object (New Serializer)
                if (item instanceof ChatMessage) {
                    ChatMessage msg = (ChatMessage) item;

                    String sender = msg.getSender();
                    String content = msg.getContent();
                    ChatMessage.MessageType type = msg.getType();

                    if (type == ChatMessage.MessageType.CHAT) {
                        Paragraph p = new Paragraph();
                        p.add(new Chunk(sender + ": ", senderFont));
                        p.add(new Chunk(content, messageFont));
                        document.add(p);
                    } else {
                        // JOIN/LEAVE events
                        document.add(new Paragraph(sender + " " + content, sysFont));
                    }
                }
                // FALLBACK: Check if it's a Map (Old Serializer or different config)
                else if (item instanceof java.util.Map) {
                    java.util.Map<?, ?> msg = (java.util.Map<?, ?>) item;
                    String sender = (String) msg.get("sender");
                    String content = (String) msg.get("content");
                    String typeStr = String.valueOf(msg.get("type"));

                    if ("CHAT".equals(typeStr)) {
                        Paragraph p = new Paragraph();
                        p.add(new Chunk(sender + ": ", senderFont));
                        p.add(new Chunk(content, messageFont));
                        document.add(p);
                    } else {
                        document.add(new Paragraph(sender + " " + content, sysFont));
                    }
                }
            }

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}