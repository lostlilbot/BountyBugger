# Bounty Sources Implementation Plan

## Current State Analysis

The BountyBugger app has the following components already in place:
- **Domain Models**: `BountyProgram`, `BountySearchFilters`, `BountySearchResult` - Complete
- **Database Layer**: `BountyDatabase`, `BountyProgramEntity`, `BountyProgramDao` - Complete
- **UI Layouts**: `activity_bounty_search.xml`, `item_bounty_program.xml` - Complete
- **Missing**: Activity implementation, API service, Repository

## Implementation Plan

### 1. Create BountyRepository
**Purpose**: Centralized data management layer that coordinates between local DB and remote API

**Location**: `data/repository/BountyRepository.kt`

**Responsibilities**:
- Fetch programs from remote API
- Cache results in local Room database
- Provide search and filter capabilities
- Manage favorites

### 2. Implement BountyApiService with Sample Data
**Purpose**: Provide real bounty program data for popular platforms

**Approach**: Since most bug bounty platforms require authentication (HackerOne, Bugcrowd), we'll implement:
- A sample data provider with pre-populated popular bounty programs
- API service structure for future integration with real APIs
- Support for: HackerOne, Bugcrowd, Google VRP, Meta BBP, Microsoft BRP, GitHub BBP

**Location**: `data/remote/BountyApiService.kt`, `data/remote/SampleBountyData.kt`

### 3. Create BountySearchActivity
**Purpose**: Main UI for searching and browsing bounty programs

**Location**: `ui/bounty/BountySearchActivity.kt`

**Features**:
- Search bar with query input
- Filter by type, industry, platform, reward range
- Sort by newest, highest reward, etc.
- Pull-to-refresh
- RecyclerView with program cards
- Favorite/unfavorite functionality
- Open program URL in browser

### 4. Create BountyProgramAdapter
**Purpose**: RecyclerView adapter for displaying bounty programs

**Location**: `ui/bounty/BountyProgramAdapter.kt`

### 5. Enable MainActivity Card
**Purpose**: Re-enable the Bounty Search navigation

**Changes**: Uncomment the BountySearchActivity launch code in MainActivity

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  ┌─────────────────┐    ┌────────────────────────────┐ │
│  │ BountySearch    │    │ BountyProgramAdapter       │ │
│  │ Activity        │───▶│                            │ │
│  └────────┬────────┘    └────────────────────────────┘ │
└───────────┼─────────────────────────────────────────────┘
            │
┌───────────┼─────────────────────────────────────────────┐
│           ▼            Repository Layer                 │
│  ┌─────────────────────────────────────────────────┐   │
│  │              BountyRepository                    │   │
│  │  - searchPrograms(filters)                       │   │
│  │  - getFavorites()                                │   │
│  │  - toggleFavorite(id)                            │   │
│  │  - refreshPrograms()                              │   │
│  └────────┬────────────────────┬────────────────────┘   │
└───────────┼────────────────────┼──────────────────────┘
            │                    │
┌───────────┼────────────────────┼──────────────────────┐
│           ▼                    ▼   Data Layer        │
│  ┌──────────────────┐  ┌────────────────────────┐   │
│  │ BountyApiService │  │  BountyProgramDao      │   │
│  │ (Sample Data)    │  │  (Room Database)       │   │
│  └──────────────────┘  └────────────────────────┘   │
└───────────────────────────────────────────────────────┘
```

## Sample Bounty Programs to Include

| Platform | Program Name | URL |
|----------|--------------|-----|
| Google VRP | Google | https://bughunter.google.com |
| Meta BBP | Facebook | https://www.facebook.com/bugbounty |
| Microsoft | Microsoft VRP | https://www.microsoft.com/msrc/bug-bounty |
| Apple | Apple BBP | https://developer.apple.com/security-bounty |
| GitHub | GitHub BBP | https://bounty.github.com |
| HackerOne | Twitter | https://hackerone.com/twitter |
| Bugcrowd | Tesla | https://bugcrowd.com/tesla |
| Intigriti | Uber | https://intigriti.com/uber |

## Next Steps

Switch to Code mode to implement:
1. BountyRepository
2. BountyApiService + SampleBountyData  
3. BountySearchActivity
4. BountyProgramAdapter
5. Enable MainActivity card
