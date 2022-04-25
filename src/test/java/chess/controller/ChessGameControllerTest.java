package chess.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import chess.controller.ChessGameControllerTest.HandlebarConfig;
import chess.domain.Score;
import chess.domain.piece.Color;
import chess.dto.ChessGameDto;
import chess.dto.GameStatus;
import chess.service.ChessGameService;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChessGameController.class)
@ContextConfiguration(classes = HandlebarConfig.class)
class ChessGameControllerTest {

    @Configuration
    class HandlebarConfig {

        @Bean
        public HandlebarsViewResolver handlebarsViewResolver() {
            HandlebarsViewResolver resolver = new HandlebarsViewResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".hbs");
            return resolver;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChessGameService chessGameService;

    private int chessGameId;

    @BeforeEach
    void setUp() {
        chessGameId = chessGameService.create("hoho");
        chessGameService.getOrSaveChessGame(chessGameId);
    }

    @Test
    @DisplayName("체스 게임 방 접속")
    void chessGame() throws Exception {
        Mockito.when(chessGameService.getOrSaveChessGame(1)).thenReturn(
            new ChessGameDto(1, "hoho", GameStatus.RUNNING, new Score(), new Score(), Color.WHITE));
        Mockito.when(chessGameService.findPieces(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/chess-game").param("chess-game-id", String.valueOf(1)))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                view().name("chess-game"),
                model().attributeExists("pieces"),
                model().attributeExists("chessGame")
            );
    }

    @Test
    @DisplayName("정상적인 기물 이동")
    void move() throws Exception {
        mockMvc.perform(post("/chess-game/move")
            .param("chess-game-id", String.valueOf(chessGameId))
            .param("from", "A2")
            .param("to", "A4"))
            .andDo(print())
            .andExpectAll(
                status().is3xxRedirection(),
                redirectedUrl("/chess-game?chess-game-id=" + chessGameId),
                flash().attributeCount(0)
            );
    }

    @Test
    @DisplayName("비정상적인 기물 이동")
    void invalidMove() throws Exception {
        mockMvc.perform(post("/chess-game/move")
            .param("chess-game-id", String.valueOf(chessGameId))
            .param("from", "A2")
            .param("to", "A5"))
            .andDo(print())
            .andExpectAll(
                status().is3xxRedirection(),
                redirectedUrl("/chess-game?chess-game-id=" + chessGameId),
                flash().attributeExists("hasError"),
                flash().attributeExists("errorMessage")
            );
    }

}
