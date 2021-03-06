# Change Log
All notable changes to the "aiXcoder" extension will be documented in this file.

## [1.3.2] - 2020-04-20
### Added
- clean some old local server files
- improve predict by file change detect, reuse maven info


## [1.3.1] - 2020-03-30
### Added
- compatible with eclipse 2020

### Fixed
- sometimes cursor position error.


## [1.2.5] - 
### Fixed
- Sometimes closes proposals popup preemptively.

## [1.2.4] - 2020-02-03
### Fixed
- A first time installation bug on OS X and Linux

## [1.2.3] - 2020-01-20

### Added
- Adds a download progress indicator.

### Fixed
- A bug that causes first time installation on Linux to fail.
- A bug that sometimes causes prediction to not show at the end of a word.
- A caret position bug when using auto import.

## [1.2.2] - 2020-01-09

### Added
- Prompts to login if previously used online service.
- Does not download/update local service if the plugin is using online service.
- Prompts to switch to local service if already using online service.

### Fixed
- No longer provide completions inside string or comments.

## [1.2.1] - 2019-12-31

### Added
- Supports new version file.

## [1.2.0] - 2019-12-31

### Change
- Merged online and local branch.

## [1.1.9] - 2019-12-16
### Fix
- Fix a bug that make service start multiple times.

## [1.1.8] - 2019-12-11
### Added
- Show local service indexing status.

### Fix
- Fix a bug that make service failed to start.


## [1.1.6] - 2019-11-21
### Fix
- (Mac) Execute permission issue

## [1.1.5] - 2019-11-21

### Add
### Change
### Fix
- Java 1.6 compliance

## [1.1.4] - 2019-11-19
### Add
- Now works better with project-scope completion.

### Change

### Fix
- Fix a bug when there is quote in comment.
- Kill local service when updating to prevent file overwrite failures.

## [1.1.3] - 2019-11-04
### 新增

### 改动

### 修复
- 老版本eclipse上的一个兼容问题
- 设置系统代理时无法使用的bug

## [1.1.2] - 2019-11-01
### 新增

### 改动
- 如果使用本地版服务，则自动启动本地版服务程序

### 修复
- 老版本eclipse上的一个兼容问题

## [1.1.1] - 2019-10-28
### 新增
- 主动提示更新
- 增加了本地版的支持

### 改动

### 修复

## [1.0.10] - 2019-08-12
### 新增

### 改动

### 修复
- 修复了在某些情况下会出现AiX Report EMPTY错误的问题。

## [1.0.9] - 2019-07-29
### 新增

### 改动

### 修复
- 修复了一个阻碍使用的严重问题。

## [1.0.8] - 2019-07-29
### 新增
- 新接口提升负载均衡速度。

### 改动

### 修复


## [1.0.7] - 2019-07-18
### 新增
- 提供多个不同长度的结果，这个行为可以在设置页面中设置开关及出现的结果的数量和顺序。

### 改动
- 现在会在设置页面中正确显示补全提供器的名称了

### 修复


## [1.0.7] - 2019-07-16
### 新增

### 改动

### 修复
- 修复了相同的import重复出现的问题。

## [1.0.6] - 2019-07-11
### Added

### Changed

### Fixed
- Fixed a compatibility issue on Eclipse 2019-06

## [1.0.5] - 2019-07-02
### Added

### Changed
- Limit sort results with the same word to the most probable single result.

### Fixed
- Fixed globe emoji 🌏 displaying.
- Removed extraneous space before right parenthesis.
- AiXCoder is properly displayed instead of obsolete "AiXSorter".
- Fixed a compatibility issue on Eclipse Oxygen or lower.

## [1.0.4] - 2019-06-11
### Added
- Warns you when endpoint is empty.
- Localized first-time install orientation.
- Added a configuration to set the preferred position of long results.

### Changed
- Removed the useless preferences in community version.

### Fixed
- Fixed an issue on Eclipse version < 3.11 when starts up.
- Fixed localization showing as corrupted on earlier version of Eclipse.


## [1.0.3] - 2019-06-06
### Added
- Java: Automatically add missing class imports in completions.
- New feature to suggest variable names during variable/parameter definition.
- Added a configuration to turn off long results (sort only).
- Chinese Simplified localized preference page (use language dropdown to select).

### Changed
- Use new telemetry API.
- No longer sort template completions to top.

### Fixed
