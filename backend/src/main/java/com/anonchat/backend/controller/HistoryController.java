package com.anonchat.backend.controller;

import com.anonchat.backend.service.ChatService;
import com.anonchat.backend.service.PdfService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class HistoryController {

    private final ChatService chatService;
    private final PdfService pdfService;

    // History API
    @GetMapping("/api/history/{roomId}")
    public ResponseEntity<List<Object>> getChatHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getHistory(roomId));
    }

    // PDF Export API
    @GetMapping("/api/export/{roomId}")
    public ResponseEntity<InputStreamResource> exportPdf(@PathVariable String roomId) {
        // Get the data
        List<Object> history = chatService.getHistory(roomId);

        // Generate the PDF
        ByteArrayInputStream pdfStream = pdfService.exportChatToPdf(roomId, history);

        // Set Headers so the browser knows it's a file download
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=chat-" + roomId + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}