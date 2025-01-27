package it.es.libri.presentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.es.libri.model.Autore;
import it.es.libri.service.AutoriService;

@Controller
@RequestMapping("/autore")
public class AutoreCtrl {

	@Autowired
	AutoriService srv;
	
	@GetMapping("/list")
	public String get(Model model) {		
		model.addAttribute("autori", srv.getAll());
		return "autori";
	}
	
	@GetMapping()
	public String getByID(@RequestParam(value="id", required=true) int autoreId, Model model) {
				
		var autore = srv.getByID(autoreId);
		model.addAttribute("autore", autore);
		
		if(autore==null)
			model.addAttribute("error", "#404 autore [id: "+autoreId+"] not found");		
		
		model.addAttribute("isUpdate", true);
		
		return "autore";
	}
	
	@GetMapping("/new")
	public String createNew(Model model) {
				
		model.addAttribute("autore", new Autore());
		
		model.addAttribute("isUpdate", false);
		
		return "autore";
	}
	
	@PostMapping("/save")
	public String save(@RequestParam(value="isUpdate") boolean isUpdate, @ModelAttribute Autore autore, Model model) {	
		if(!isUpdate) {
			autore.setId(0);
			model.addAttribute("message", srv.add(autore));			
		}
		else
			model.addAttribute("message", srv.update(autore));			
			
		return "redirect:/autore/list";
	}

	@DeleteMapping()
	public String delete(@RequestParam(value="id", required=true) int autoreId, Model model) {
		var autore = new Autore();
		autore.setId(autoreId);
		model.addAttribute("message", this.srv.remove(autore));
		return this.get(model);
	}
	
	@GetMapping("/export")
	public ResponseEntity<InputStreamResource> download(String param) throws IOException {

		String outFile = this.srv.exportCsv();

		if (outFile != null) {
			File download = new File(outFile);
			InputStreamResource resource = new InputStreamResource(new FileInputStream(download));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=exportArticolo.csv")
					.contentLength(download.length())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(resource);
		}

		return ResponseEntity.notFound().build();
	}

	@PostMapping("/import")
	public ResponseEntity<?> insertCSV(@RequestParam("fileCSV") MultipartFile file) {

		this.srv.importCsv(file);
		return ResponseEntity.ok().build();
		
	}
}
