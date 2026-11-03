package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.RoomFavorite;
import com.example.meeting.mapper.RoomFavoriteMapper;
import com.example.meeting.service.RoomFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会议室收藏服务实现类
 */
@Service
public class RoomFavoriteServiceImpl extends ServiceImpl<RoomFavoriteMapper, RoomFavorite> implements RoomFavoriteService {

    @Autowired
    private RoomFavoriteMapper roomFavoriteMapper;

    @Override
    public List<Long> getFavoriteRoomIds(Long userId) {
        List<RoomFavorite> favorites = roomFavoriteMapper.selectByUserId(userId);
        return favorites.stream().map(RoomFavorite::getRoomId).collect(Collectors.toList());
    }

    @Override
    public void addFavorite(Long userId, Long roomId) {
        if (!isFavorited(userId, roomId)) {
            RoomFavorite favorite = new RoomFavorite();
            favorite.setUserId(userId);
            favorite.setRoomId(roomId);
            favorite.setSort(0);
            save(favorite);
        }
    }

    @Override
    public void removeFavorite(Long userId, Long roomId) {
        lambdaUpdate()
                .eq(RoomFavorite::getUserId, userId)
                .eq(RoomFavorite::getRoomId, roomId)
                .remove();
    }

    @Override
    public boolean isFavorited(Long userId, Long roomId) {
        return roomFavoriteMapper.countByUserAndRoom(userId, roomId) > 0;
    }
}
