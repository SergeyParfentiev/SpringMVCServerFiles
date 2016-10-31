package ua.kiev.prog;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/")
public class MyController {

    private Map<Long, byte[]> photos = new HashMap<>();
    private Map<Long, String> photosNames = new HashMap<>();

    @RequestMapping("/")
    public String onIndex() {
        return "index";
    }

    @RequestMapping(value = "/add_photo", method = RequestMethod.POST)
    public String onAddPhoto(Model model, @RequestParam MultipartFile photoT) {
        System.out.println(photoT.getOriginalFilename());
        if (photoT.isEmpty()) {
            throw new FileErrorException();
        }

        try {
            long id = System.currentTimeMillis();

            photos.put(id, photoT.getBytes());
            photosNames.put(id, photoT.getOriginalFilename());

            model.addAttribute("photo_id", id);
            return "result";
        } catch (Exception e) {
            throw new FileErrorException();
        }
    }

    @RequestMapping(value = "/photosList")
    public String photosList(Model model) {
        model.addAttribute("photos", photos);
        return "photosList";
    }


    @RequestMapping(value = "/selectPhotos", method = RequestMethod.POST)
    public String deletePhotos(HttpServletResponse response, @RequestParam(value = "checkboxName",
            required = false) long[] checkboxValue, @RequestParam("choose") String choose) {
        try {
            if (null != checkboxValue) {
                String delete = "Delete selected";
                if (delete.equals(choose)) {
                    for (Long aLong : checkboxValue) {
                        photos.remove(aLong);
                        photosNames.remove(aLong);
                    }
                } else {
                    response.setContentType("application/zip");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.addHeader("Content-Disposition", "attachment; filename=\"photos.zip\"");

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

                    for (Long aLong : checkboxValue) {
                        zipOutputStream.putNextEntry(new ZipEntry(photosNames.get(aLong)));
                        InputStream InputStream = new ByteArrayInputStream(photos.get(aLong));
                        IOUtils.copy(InputStream, zipOutputStream);

                        InputStream.close();
                        zipOutputStream.closeEntry();
                    }
                    zipOutputStream.finish();
                    zipOutputStream.flush();

                    IOUtils.closeQuietly(zipOutputStream);
                    IOUtils.closeQuietly(byteArrayOutputStream);

                    InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    FileCopyUtils.copy(is, response.getOutputStream());
                    response.getOutputStream().flush();
                }
            }
        } catch (Exception e) {
            throw new FileErrorException();
        }
        return "index";
    }

    @RequestMapping("/photo/{photo_id}")
    public ResponseEntity<byte[]> onPhoto(@PathVariable("photo_id") long id) {
        return photoById(id);
    }

    @RequestMapping(value = "/view", method = RequestMethod.POST)
    public ResponseEntity<byte[]> onView(@RequestParam("photo_id") long id) {
        return photoById(id);
    }

    @RequestMapping("/delete/{photo_id}")
    public String onDelete(@PathVariable("photo_id") long id) {

        if (photos.remove(id) == null)
            throw new FileNotFoundException();
        else {
            return "index";
        }
    }

    private ResponseEntity<byte[]> photoById(long id) {
        byte[] bytes = (photos.get(id));
        if (bytes == null)
            throw new FileNotFoundException();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
    }
}
