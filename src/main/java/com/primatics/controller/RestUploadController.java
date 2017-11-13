package com.primatics.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Stopwatch;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

@RestController
public class RestUploadController {

	private final Logger logger = LoggerFactory.getLogger(RestUploadController.class);
	private final GridFsTemplate gridFsTemplate;
	ResponseEntity<Integer> numOfFiles;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	public RestUploadController(GridFsTemplate gridFsTemplate) {
		this.gridFsTemplate = gridFsTemplate;
	}

	// Multiple file upload
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/api/upload/multi")
	public ResponseEntity<?> uploadFileMulti(@RequestParam("extraField") String extraField,
			@RequestParam("files") MultipartFile[] uploadfiles) {
		String run_name = "";
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
			System.out.println("**************************************"+run_name);
			numOfFiles = restTemplate.getForEntity("http://cecl-poc-batch-service-cecl-poc.router.default.svc.cluster.local/runjob/split/" + run_name, Integer.class);
			stopped = watch.stop();
			long heapSize = Runtime.getRuntime().totalMemory();
	        System.out.println("UPLOAD CONTROLLER - Heap Size = " + heapSize + " - Time: "+stopped);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity("Successfully uploaded - " + uploadedFileName + " -- " + numOfFiles.getBody()
				+ " Loans Parsed for Run: " + run_name + " in " + stopped, new HttpHeaders(), HttpStatus.OK);

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
		Optional<GridFSDBFile> existing = maybeLoadFile(runName);
		if (existing.isPresent()) {
			runName = "DUPLICATE_" + runName;
		}
		DBObject metadata = new BasicDBObject();
		metadata.put("run_name", runName);
		System.out.println("************ In create or Update method for saving file");
		try {
		System.out.println("************ In create or Update method for saving file - BEFORE STORE");
		gridFsTemplate.store(file.getInputStream(), name, file.getContentType(), metadata).save();
		} catch (Exception e) {
			System.out.println("************ In create or Update method for saving file - EXCEPTION");
			logger.error(e.getMessage());
		}
		String resp = "<script>window.location = '/';</script>";

		return new HttpEntity<>(resp.getBytes());
	}

	private static Query getFilenameQuery(String name) {
		return Query.query(GridFsCriteria.whereMetaData().is(name));
	}

	private Optional<GridFSDBFile> maybeLoadFile(String name) {
		GridFSDBFile file = gridFsTemplate.findOne(getFilenameQuery(name));
		return Optional.ofNullable(file);
	}
}
