package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.RoomFavorite;

import java.util.List;

/**
 * 会议室收藏服务接口
 */
public interface RoomFavoriteService extends IService<RoomFavorite> {

    /**
     * 获取用户收藏的会议室ID列表
     */
    List<Long> getFavoriteRoomIds(Long userId);

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, Long roomId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Long roomId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long userId, Long roomId);
}
