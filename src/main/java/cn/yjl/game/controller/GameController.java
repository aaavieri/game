package cn.yjl.game.controller;

import cn.yjl.game.dto.ResponseJsonDto;
import cn.yjl.game.dto.event.BaseEventDto;
import cn.yjl.game.dto.event.GameEventWrapDto;
import cn.yjl.game.dto.request.BaseRequestDto;
import cn.yjl.game.dto.request.DoPlayRequestDto;
import cn.yjl.game.dto.request.LoginRequestDto;
import cn.yjl.game.listener.GameListener;
import cn.yjl.game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameListener gameListener;

    @PostMapping("/login")
    public ResponseJsonDto login(@RequestBody LoginRequestDto requestDto) {
        return new ResponseJsonDto();
    }

    @GetMapping("/gameEvent/{userId}")
    public SseEmitter gameEvent(@PathVariable String userId) {
        return this.gameListener.registerEvent(userId);
    }

    @PostMapping("/joinGame")
    public ResponseJsonDto joinGame(@RequestBody BaseRequestDto requestDto) {
        GameEventWrapDto data = this.gameService.joinGame(requestDto);
        return new ResponseJsonDto().setData(data.getGameId()).addClaim("eventData",
                data.getEventData().stream().filter(eventData -> requestDto.getUserId().equals(eventData.getUserId()))
                        .findFirst().orElse(new BaseEventDto()));
    }

    @PostMapping("/startGame")
    public ResponseJsonDto startGame(@RequestBody BaseRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.startGame(requestDto).getGameId());
    }

    @PostMapping("/skipLord")
    public ResponseJsonDto skipLord(@RequestBody BaseRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.skipLord(requestDto).getGameId());
    }

    @PostMapping("/callLord")
    public ResponseJsonDto callLord(@RequestBody BaseRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.callLord(requestDto).getGameId());
    }

    @PostMapping("/skipPlay")
    public ResponseJsonDto skipPlay(@RequestBody BaseRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.skipPlay(requestDto).getGameId());
    }

    @PostMapping("/doPlay")
    public ResponseJsonDto doPlay(@RequestBody DoPlayRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.doPlay(requestDto).getGameId());
    }

    @PostMapping("/restart")
    public ResponseJsonDto restart(@RequestBody DoPlayRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.restart(requestDto).getGameId());
    }

    @PostMapping("/quitGame")
    public ResponseJsonDto quitGame(@RequestBody BaseRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.quitGame(requestDto).getGameId());
    }

    //  TODO FOR TEST
    @GetMapping("/resetGame/{gameId}")
    public ResponseJsonDto resetGame(@PathVariable int gameId) {
        return new ResponseJsonDto().setData(this.gameService.resetGame(gameId).getGameId());
    }

    @GetMapping("/resetAll")
    public ResponseJsonDto resetAll() {
        this.gameService.resetAll();
        return new ResponseJsonDto().setData(null);
    }
}
