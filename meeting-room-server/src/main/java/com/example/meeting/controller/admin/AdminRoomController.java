package com.example.meeting.controller.admin;

import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.Result;
import com.example.meeting.dto.AdminBookingDTO;
import com.example.meeting.dto.BookingQueryDTO;
import com.example.meeting.dto.RoomDTO;
import com.example.meeting.service.BookingService;
import com.example.meeting.service.MeetingRoomService;
import com.example.meeting.vo.BookingVO;
import com.example.meeting.vo.RoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理 - 会议室管理
 */
@Tag(name = "后台管理 - 会议室管理")
@RestController
@RequestMapping("/api/v1/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final MeetingRoomService meetingRoomService;

    @Operation(summary = "会议室列表")
    @GetMapping
    public Result<List<RoomVO>> getRoomList(
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Integer status) {
        List<RoomVO> rooms = meetingRoomService.getRoomList(floor, capacity, status);
        return Result.success(rooms);
    }

    @Operation(summary = "新增会议室")
    @PostMapping
    public Result<RoomVO> createRoom(@Valid @RequestBody RoomDTO dto) {
        RoomVO room = meetingRoomService.createRoom(dto);
        return Result.success(room);
    }

    @Operation(summary = "编辑会议室")
    @PutMapping("/{id}")
    public Result<RoomVO> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDTO dto) {
        RoomVO room = meetingRoomService.updateRoom(id, dto);
        return Result.success(room);
    }

    @Operation(summary = "删除会议室")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        meetingRoomService.deleteRoom(id);
        return Result.success();
    }

    @Operation(summary = "修改会议室状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateRoomStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        meetingRoomService.updateRoomStatus(id, status);
        return Result.success();
    }
}
