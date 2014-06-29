parsed = LOAD '/user/hduser/tsv/wiki.xml' USING pignlproc.storage.ParsingWikipediaLoader('en') AS (title, id, pageUrl, text, redirect, links, headers, paragraphs);
DUMP parsed;