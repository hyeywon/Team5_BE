package ojosama.talkak.video.controller;

import static org.hibernate.query.sqm.tree.SqmNode.log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import ojosama.talkak.reaction.service.ReactionService;
import ojosama.talkak.video.request.VideoCategoryRequest;
import ojosama.talkak.video.request.VideoRequest;
import ojosama.talkak.video.request.YoutubeCategoryRequest;
import ojosama.talkak.video.request.YoutubeUrlValidationRequest;
import ojosama.talkak.video.response.VideoDetailsResponse;
import ojosama.talkak.video.response.VideoInfoResponse;
import ojosama.talkak.video.response.VideoResponse;
import ojosama.talkak.video.response.YoutubeApiResponse;
import ojosama.talkak.video.response.YoutubeUrlValidationResponse;
import ojosama.talkak.video.service.AwsS3Service;
import ojosama.talkak.video.service.VideoService;
import ojosama.talkak.video.service.YoutubeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@RestController
@RequestMapping("/api/videos")
public class VideoController implements VideoApiController {

    private final VideoService videoService;
    private final YoutubeService youtubeService;
    private final AwsS3Service awsS3Service;
    private final ReactionService reactionService;

    public VideoController(VideoService videoService, YoutubeService youtubeService,
                           AwsS3Service awsS3Service, ReactionService reactionService) {
        this.videoService = videoService;
        this.youtubeService = youtubeService;
        this.awsS3Service = awsS3Service;
        this.reactionService = reactionService;
    }

    @GetMapping
    public ResponseEntity<List<VideoInfoResponse>> getPopularVideosByCategory(@RequestBody VideoCategoryRequest req,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<VideoInfoResponse> videos = videoService.getVideoByCategory(req, pageable);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoDetailsResponse> getVideoDetails(@PathVariable Long videoId) {
        VideoDetailsResponse response = videoService.getVideoDetailsByVideoId(videoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<VideoResponse> uploadShortsVideo(
            @RequestParam("file") MultipartFile file, VideoRequest videoRequest) throws IOException {
        VideoResponse response = awsS3Service.uploadVideo(file, videoRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{videoId}/extract")
    public ResponseEntity<URL> downloadVideo(@PathVariable Long videoId)
            throws MalformedURLException {
        URL downloadUrl = awsS3Service.generateDownloadUrl(videoId);
        return ResponseEntity.ok(downloadUrl);
    }

    @PostMapping("/youtube-url-validation")
    public ResponseEntity<YoutubeUrlValidationResponse> validateYoutubeUrl(
            @RequestBody YoutubeUrlValidationRequest req) {
        YoutubeUrlValidationResponse response = videoService.validateYoutubeUrl(req);
        return ResponseEntity.ok(response);
    }

    // 메인페이지에서 유튜브 관련 영상 불러오기(카테고리 지정 X)
    @GetMapping("/youtube")
    public ResponseEntity<List<YoutubeApiResponse>> getPopularYoutubeShorts() throws IOException {
        long start = System.currentTimeMillis();
        List<YoutubeApiResponse> response = youtubeService.getPopularShorts();
        long end = System.currentTimeMillis();
        log.info("인기 쇼츠 Cache 수행시간 : " + (end - start));

        return ResponseEntity.ok(response);
    }

    // 메인페이지에서 유튜브 관련 영상 불러오기(카테고리 지정)
    @GetMapping("/youtube/{categoryId}")
    public ResponseEntity<List<YoutubeApiResponse>> getPopularYoutubeShortsByCategory(
            @PathVariable("categoryId")
            YoutubeCategoryRequest youtubeCategoryRequest) throws IOException {
        long start = System.currentTimeMillis();
        List<YoutubeApiResponse> response = youtubeService.getShortsByCategory(
                youtubeCategoryRequest);
        long end = System.currentTimeMillis();
        log.info("카테고리별 쇼츠 Cache 수행시간 : " + (end - start));

        return ResponseEntity.ok(response);
    }

}