package com.example.pleasework.controller;



import com.example.pleasework.entity.Template;
import com.example.pleasework.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/files")
public class TemplateFileController {

    @Autowired
    private TemplateRepository templateRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> getTemplateFile(@PathVariable Integer id) {
        Optional<Template> templateOpt = templateRepository.findById(id);
        if (templateOpt.isPresent()) {
            Template template = templateOpt.get();

            ByteArrayResource resource = new ByteArrayResource(template.getFile());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(template.getFilename() != null ? template.getFilename() : "template.xlsx")
                    .build());

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

