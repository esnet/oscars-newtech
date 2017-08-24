package net.es.oscars.app.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Properties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitRepositoryState {
    String tags;                    // =${git.tags} // comma separated tag names
    String branch;                  // =${git.branch}
    String dirty;                   // =${git.dirty}
    String remoteOriginUrl;         // =${git.remote.origin.url}

    String commitId;                // =${git.commit.id.full} OR ${git.commit.id}
    String commitIdAbbrev;          // =${git.commit.id.abbrev}
    String describe;                // =${git.commit.id.describe}
    String describeShort;           // =${git.commit.id.describe-short}
    String commitUserName;          // =${git.commit.user.name}
    String commitUserEmail;         // =${git.commit.user.email}
    String commitMessageFull;       // =${git.commit.message.full}
    String commitMessageShort;      // =${git.commit.message.short}
    String commitTime;              // =${git.commit.time}
    String closestTagName;          // =${git.closest.tag.name}
    String closestTagCommitCount;   // =${git.closest.tag.commit.count}

    String buildUserName;           // =${git.build.user.name}
    String buildUserEmail;          // =${git.build.user.email}
    String buildTime;               // =${git.build.time}
    String buildHost;               // =${git.build.host}
    String buildVersion;            // =${git.build.version}

    public GitRepositoryState(Properties properties)
    {
        this.tags = String.valueOf(properties.get("git.tags"));
        this.branch = String.valueOf(properties.get("git.branch"));
        this.dirty = String.valueOf(properties.get("git.dirty"));
        this.remoteOriginUrl = String.valueOf(properties.get("git.remote.origin.url"));

        this.commitId = String.valueOf(properties.get("git.commit.id.full")); // OR properties.get("git.commit.id") depending on your configuration
        this.commitIdAbbrev = String.valueOf(properties.get("git.commit.id.abbrev"));
        this.describe = String.valueOf(properties.get("git.commit.id.describe"));
        this.describeShort = String.valueOf(properties.get("git.commit.id.describe-short"));
        this.commitUserName = String.valueOf(properties.get("git.commit.user.name"));
        this.commitUserEmail = String.valueOf(properties.get("git.commit.user.email"));
        this.commitMessageFull = String.valueOf(properties.get("git.commit.message.full"));
        this.commitMessageShort = String.valueOf(properties.get("git.commit.message.short"));
        this.commitTime = String.valueOf(properties.get("git.commit.time"));
        this.closestTagName = String.valueOf(properties.get("git.closest.tag.name"));
        this.closestTagCommitCount = String.valueOf(properties.get("git.closest.tag.commit.count"));

        this.buildUserName = String.valueOf(properties.get("git.build.user.name"));
        this.buildUserEmail = String.valueOf(properties.get("git.build.user.email"));
        this.buildTime = String.valueOf(properties.get("git.build.time"));
        this.buildHost = String.valueOf(properties.get("git.build.host"));
        this.buildVersion = String.valueOf(properties.get("git.build.version"));
    }

}
