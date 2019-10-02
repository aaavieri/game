package cn.yjl.game.controller;

import cn.yjl.game.dto.ResponseJsonDto;
import cn.yjl.game.dto.request.BaseRequestDto;
import cn.yjl.game.dto.request.LoginRequestDto;
import cn.yjl.game.listener.GameListener;
import cn.yjl.game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/gameEvent")
    public SseEmitter gameEvent(@RequestBody BaseRequestDto requestDto) {
        return this.gameListener.registerEvent(requestDto.getUserId());
    }

    @PostMapping("/joinGame")
    public ResponseJsonDto joinGame(@RequestBody BaseRequestDto requestDto) {
        return new ResponseJsonDto().setData(this.gameService.joinGame(requestDto).getGameId());
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
        return new ResponseJsonDto().setData(this.gameService.startGame(requestDto).getGameId());
    }
}
