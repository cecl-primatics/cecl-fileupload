package com.primatics.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Stopwatch;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.primatics.model.RawData;

@RestController
public class RestUploadController {

	private final Logger logger = LoggerFactory.getLogger(RestUploadController.class);

	private final GridFsTemplate gridFsTemplate;
	
	@Autowired
	RestTemplate restTemplate;

	ResponseEntity<Integer> numOfFiles;
	
	public static String run_name = "";
	
	@Autowired
	public RestUploadController(GridFsTemplate gridFsTemplate) {
		this.gridFsTemplate = gridFsTemplate;
	}
	// Multiple file upload
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/api/upload/multi")
	public ResponseEntity<?> uploadFileMulti(@RequestParam("extraField") String extraField,
			@RequestParam("files") MultipartFile[] uploadfiles) {

		logger.debug("Multiple file upload!");
		Stopwatch stopped;
		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
				.filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));
		run_name = extraField + "_" + uploadedFileName.substring(0, uploadedFileName.lastIndexOf('.'));
		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity("please select a file!", HttpStatus.OK);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfiles), extraField);
			Stopwatch watch = Stopwatch.createStarted();
			numOfFiles = restTemplate.getForEntity("http://localhost:8083/runjob/split/"+run_name, Integer.class);
			stopped = watch.stop();
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity("Successfully uploaded - " + uploadedFileName + " -- "+numOfFiles.getBody()+" Loans Parsed for Run: "+run_name+ " in "+stopped, new HttpHeaders(),
				HttpStatus.OK);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/api/calculate")
	public ResponseEntity<?> calculate() {

		logger.debug("CALCULATE");
		Stopwatch stopped = Stopwatch.createStarted();		
		ResponseEntity<String> start = restTemplate.getForEntity("http://localhost:8082/api/start", String.class);
		stopped = stopped.stop();
		
		logger.debug("IMPORT");
		Stopwatch imp = Stopwatch.createStarted();		
		restTemplate.getForEntity("http://localhost:8082/api/import", Stopwatch.class);
		imp = imp.stop();
		
		Stopwatch imp1 = Stopwatch.createStarted();
		restTemplate.getForEntity("http://localhost:8082/api/cache", Stopwatch.class);
		imp1 = imp1.stop();
		
		return new ResponseEntity("Client Start "+start.getBody()+" in "+stopped+" and Cache import took "+imp+" Calculation done in "+imp1, new HttpHeaders(),
				HttpStatus.OK);

	}
	
	// save file
	private HttpEntity<byte[]> saveUploadedFiles(List<MultipartFile> files, String extraField) throws IOException {

		HttpEntity<byte[]> resp = null;
		for (MultipartFile file : files) {

			if (file.isEmpty()) {
				continue; // next pls
			}

			resp = createOrUpdate(file, extraField);
		}
		return resp;
	}

	public HttpEntity<byte[]> createOrUpdate(MultipartFile file, String extraField) {
		String name = file.getOriginalFilename();
		String runName = extraField + "_" + name.substring(0, name.lastIndexOf('.'));
		try {
			Optional<GridFSDBFile> existing = maybeLoadFile(name);
			if (existing.isPresent()) {
				runName = "DUPLICATE_"+runName;
			}
			DBObject metadata = new BasicDBObject();
			metadata.put("run_name", runName);
			gridFsTemplate.store(file.getInputStream(), name, file.getContentType(), metadata).save();
			String resp = "<script>window.location = '/';</script>";
			
			return new HttpEntity<>(resp.getBytes());
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static Query getFilenameQuery(String name) {
		return Query.query(GridFsCriteria.whereFilename().is(name));
	}

	private Optional<GridFSDBFile> maybeLoadFile(String name) {
		GridFSDBFile file = gridFsTemplate.findOne(getFilenameQuery(name));
		return Optional.ofNullable(file);
	}

	@GetMapping("/files")
	public @ResponseBody List<String> list() {
		return getFiles().stream().map(GridFSDBFile::getFilename).collect(Collectors.toList());
	}

	@GetMapping("/{name:.+}")
	public HttpEntity<byte[]> get(@PathVariable("name") String name) {
		try {
			Optional<GridFSDBFile> optionalCreated = maybeLoadFile(name);
			if (optionalCreated.isPresent()) {
				GridFSDBFile created = optionalCreated.get();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				created.writeTo(os);
				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_TYPE, created.getContentType());
				return new HttpEntity<>(os.toByteArray(), headers);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.IM_USED);
		}
	}
	
	private List<GridFSDBFile> getFiles() {
	    return gridFsTemplate.find(null);
	  }
}
