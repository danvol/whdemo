-- phpMyAdmin SQL Dump
-- version 3.2.5
-- http://www.phpmyadmin.net
--
-- ホスト: mysql01.dataweb-ad.jp
-- 生成時間: 2013 年 12 月 07 日 21:02
-- サーバのバージョン: 5.1.44
-- PHP のバージョン: 5.3.1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- データベース: `research`
--

-- --------------------------------------------------------

--
-- テーブルの構造 `dtb_category`
--

CREATE TABLE IF NOT EXISTS `dtb_category` (
  `category_id` int(11) NOT NULL,
  `category_name` text,
  `parent_category_id` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL,
  `rank` int(11) DEFAULT NULL,
  `creator_id` int(11) NOT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `del_flg` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- テーブルのデータをダンプしています `dtb_category`
--

INSERT INTO `dtb_category` (`category_id`, `category_name`, `parent_category_id`, `level`, `rank`, `creator_id`, `create_date`, `update_date`, `del_flg`) VALUES
(1, '食品', 0, 1, 6, 2, '2013-12-03 23:02:36', '2013-12-03 23:02:36', 0),
(2, '雑貨', 0, 1, 2, 2, '2013-12-03 23:02:36', '2013-12-03 23:02:36', 0),
(3, 'お菓子', 1, 2, 4, 2, '2013-12-03 23:02:36', '2013-12-03 23:02:36', 0),
(4, 'なべ', 1, 2, 5, 2, '2013-12-03 23:02:36', '2013-12-03 23:02:36', 0),
(5, 'アイス', 3, 3, 3, 2, '2013-12-03 23:02:36', '2013-12-03 23:02:36', 0),
(6, 'レシピ', 0, 1, 1, 2, '2013-12-03 23:02:36', '2013-12-03 23:02:36', 0);
