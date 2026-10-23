package com.itzbreadloaf.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;

/** Lily's books (update 2, part 9), all signed by ImLilyLol. Written by tools/lily_books.py: edit the text there. */
public final class LilyBooks {
	public static final String AUTHOR = "ImLilyLol";
	private static final Map<String, Book> BOOKS = new LinkedHashMap<>();

	public record Book(String id, String title, List<String> pages) {
	}

	private LilyBooks() {
	}

	private static void book(String id, String title, String... pages) {
		BOOKS.put(id, new Book(id, title, List.of(pages)));
	}

	static {
		book("how_it_began", "How It All Began", "Everyone asks me how ItzBreadLoaf and FelixSyn started fighting. I was there, sort of. I was hiding behind a hay bale.", "It was a sunny day in a field not far from the first bakery. Bread had baked a loaf for the whole village. FelixSyn had just found his first mace.", "He wanted to test it. He jumped really high, came down really hard... and landed right on the loaf. There were crumbs everywhere. Even in my hair.", "Bread said it was on purpose. Felix said it was an accident. They have been arguing about it ever since. I think they both kind of enjoy it now.", "If you stand where it happened, you can still see the craters. Somebody left their old swords there, and the two banners. Red for Bread, blue for Felix.");
		book("bread_argument", "The Bread Argument", "Things they have argued about, in order:\n1. The loaf.\n2. Whose fault the loaf was.\n3. Whether maces are fair.\n4. Whether bread is better toasted.", "5. Who won the last fight.\n6. Who won the fight before that.\n7. Whether it counts if you fall off a tower by yourself.", "Madison says I should stop keeping this list. But it keeps getting longer, so somebody has to.");
		book("first_oven", "The First Oven", "Before the villages had big bakeries, ItzBreadLoaf had one little brick oven, a counter, and a lot of hay.", "He baked from sunrise to sunset. When he burned a loaf, he ate it himself so nobody would know. Everybody knew. The smoke was very obvious.", "The roof is gone now and the walls are falling down. But the oven is still there, cold, against the back wall. I sat next to it to write this.", "If you find the ruins, please don't break the oven. It's kind of important to us.");
		book("torn_recipe", "Recipe Card, Torn", "Bread's very first recipe, copied as well as I could. The card was half torn.\n\n3 wheat\n1 pinch of patience\n...\nbake until golden (NOT black)", "The bottom half is missing. Bread says the secret ingredient was on it. Madison says the secret ingredient is just more wheat. I don't know who to believe.");
		book("boy_who_trained", "The One Who Trained Too Hard", "Before he was Master BreadLoaf, he was just a BreadLoaf who couldn't stop training. Every morning he hit the same old dummy in his yard.", "He trained so much that nobody in the overworld could beat him anymore. Not even FelixSyn. That made him a little sad, I think.", "His old home is still standing. It's dusty. There is gold trim on his favorite chair, and drawings of a purple portal and a strange pale land on the wall.", "He left his books behind. He left the dummy too. I think he wanted somebody else to keep practicing.");
		book("sketches_other_worlds", "Sketches of Other Worlds", "These are copies of drawings I found in Master BreadLoaf's old home.\n\nDrawing 1: a frame of obsidian with purple light inside.", "Drawing 2: a pale island floating in black sky, with tall dark towers.\n\nDrawing 3: a mace, but huge. Underneath he wrote: 'stronger'.", "I don't think he was scared of those places. I think he was excited.");
		book("why_he_left", "Why He Left", "Master BreadLoaf left from a stone platform with a broken portal and a ring of strange frames. I found it by following his footprints and a lot of guessing.", "He left a note on a lectern. It said he needed someone stronger to fight. The overworld wasn't enough.", "So he went to the Nether, where everything is hot and loud. Then he went to the End, where everything is quiet and very, very far down.", "People say he waits there in an arena for anyone brave enough. I hope he isn't lonely. If you see him, maybe say hi before the fight starts.");
		book("giant_loaf_story", "The Story of the Giant Loaf", "Once, ItzBreadLoaf decided to bake the biggest loaf in the world. Everybody helped. Madison brought wheat. I brought... moral support.", "It was so big it wouldn't fit in any oven, so they baked it outside with lots and lots of campfires. It took all week.", "Then they built a giant wagon to move it. The wagon broke. One wheel fell right off. The loaf rolled the rest of the way by itself.", "Where it stopped is where it stayed. The broken wagon is still out there on an old road, with hay spilled everywhere.", "Some people say it wasn't the first giant loaf. There was an older one, smaller, that crumbled. I think there is something under it.");
		book("under_the_loaf", "What's Under the Old Loaf", "The old loaf is mostly crust now. You can walk inside it. It smells like bread, which is nice.", "There is a red carpet in the middle. That's weird, right? Why would a loaf have a carpet?", "I lifted it up. There was a trapdoor and a ladder going down, down, down. At the bottom is a whole archive. Bookshelves, candles, and this lectern.", "I think the bakers kept their records here. I've started keeping mine here too. You're reading one of them!");
		book("archive_index", "Archive Index", "Index of places, for anyone who wants to find them all:\n\n- The Old Battle Site\n- Ruins of the First Bakery\n- Master's Old Home", "- Master's Departure Point\n- The Lost Loaf Wagon\n- This archive, under the old loaf\n- The Memorial Grove\n- The Sealed Library", "- The Cartographer's Tent\n- The Crossroads Signpost\n- The Whispering Stones\n- The Drifters' Rest\n- My naturalist camp", "- The cousins' old treehouse\n- My old writing cabin\n\nThat's fifteen. If you visit all of them, you'll know everything I know.");
		book("growing_up", "Growing Up With Madison", "Madison is my cousin. She is louder than me, braver than me, and much better at farming. I am better at reading. We decided that was fair.", "When we were little, we built a treehouse in a giant tree. We each had a tiny bed, mine pink and hers yellow. We hung our banners on the wall.", "Madison played the note block until everyone in the forest knew where we were. I read stories out loud until she fell asleep.", "We don't fit in the little beds anymore. But sometimes we still go up there. Just to sit.");
		book("height_marks", "Height Marks", "On the trunk of our treehouse tree there are two marks. Pink wool is me. Yellow wool is Madison.", "Madison's mark is higher. She says it's because she's taller. I say it's because she stood on her toes. We will never agree on this.");
		book("two_drifters", "Two Drifters", "FelixSyn and Harley don't really have a home. They drift from village to village, wherever the road goes.", "Harley is Felix's best friend. She is quiet, quick, and always has her blade. Felix talks enough for both of them.", "When they stop for the night they make a little camp: two bedrolls, a campfire, and their banners on an old stump.", "I found one of their old camps by a road. The campfire was cold. There were scorch marks where Felix practiced wind charges. Of course there were.", "I think drifting sounds lonely. But they're never alone, so maybe it isn't.");
		book("harleys_blade_note", "A Note About Harley's Blade", "Harley's blade is special. She never lets anyone else hold it. Not even Felix.", "I asked her where it came from. She smiled and said, 'A long way from here.' Then she walked off. That was the whole conversation.", "So now I really want to know.");
		book("notes_creatures", "Lily's Notes: Creatures", "Creepers: they don't want to hug you, no matter how close they get. Do not hug back.", "Endermen: don't look at them. I looked once by accident and said sorry. It did not accept my apology.", "Bees: very polite if you are polite. Madison has a whole cottage full of them.", "Breezes: they live in trial chambers and bounce around. FelixSyn loves them because they drop wind charges. I find them a bit rude.");
		book("notes_animals", "Lily's Notes: Friendly Animals", "Sheep: soft. Excellent listeners.\nCows: Madison names every single one.\nChickens: chaos.", "Axolotls: the cutest thing I have ever seen. I sat by a lush cave pond for an hour just watching one.", "Foxes: they stole my sandwich. I'm still a little upset, but they're too cute to stay mad at.");
		book("notes_nether", "Lily's Notes: The Nether", "Hot. Very hot. Bring fire resistance. Also bring a friend who isn't scared, because I was.", "Striders: they walk on lava and they look grumpy all the time. Pull them on a warped fungus and they cheer up a little.", "Piglins love gold. If you aren't wearing any, they get grumpy too. Everything in the Nether is grumpy, I think.");
		book("notes_end", "Lily's Notes: The End", "The End is very quiet. The sky is black, and the ground is pale and a little soft-looking but not soft.", "Chorus plants grow there. Eating the fruit teleports you a few blocks. I tried it and ended up behind a pillar. I stayed there a while to calm down.", "Master BreadLoaf is out there somewhere. I could hear the sound of a mace, far away.");
		book("map_notes", "The Cartographer's Notes", "Whoever lived in this tent made a map of the world on the floor out of carpet. Green for forests. Blue for water. Yellow for... maybe deserts? Or bread.", "There's an X on a carpet near a road crossing. I think it means the crossroads signpost. Go there and look at the banners.");
		book("which_way", "Which Way?", "Four banners, four ways.\n\nRed points to where the battle was.\nBlue points to where Felix rests.\nPink points to where I used to live.\nYellow points to Madison.", "Well, that's what they meant when somebody put them up. The signpost has turned a lot since then. You'll have to explore to find out.");
		book("strange_note", "A Strange Note", "I found this circle of mossy stones in a dark forest. There is a candle in the middle that never seems to go out.", "Somebody had already left a note here. It said: 'The library has no door. Look at the bushes.'", "I don't know who wrote it. The handwriting looks a little like mine, but I don't remember writing it. That's... a little spooky.");
		book("library_secret", "The Library's Secret", "If you're reading this, you found the way in! Through the bush, down the hatch, under the wall and up through the floor. Good job.", "This library has no door because whoever built it really didn't want visitors. Sorry, whoever built it.", "Most of these books are about the old days. The best ones I moved to the archive under the old loaf. Go look if you can find it.");
		book("why_i_write", "Why I Write", "I'm not very good at talking. When there are a lot of people, my words get stuck somewhere.", "But when I write, they come out fine. So I write everything down. Things I see, things people say, things I wish I had said.", "This was my cabin when I wrote most of these books. It's old now. The door is gone and moss is everywhere. I still visit.", "If you've found some of my books, thank you for reading them. That means a lot. More than I could say out loud.");
		book("shy_diary", "Diary (Please Do Not Read)", "Okay, you're reading it anyway. That's fine. I knew someone would.", "Today Madison made me say hi to a whole village. I said hi to one villager. It went 'hmm.' I'm counting that as a success.", "Tomorrow I'm going to try two villagers.");
		book("for_those_we_miss", "For Those We Miss", "This grove is for everyone who used to be here and isn't anymore. Old friends who moved far away. Pets who wandered off. The old baker's cat.", "We planted the blossom trees in a circle so it would feel like a hug. The lilies of the valley were Madison's idea.", "If you sit on the bench and stay quiet, it feels peaceful. You can light a candle if you want to. I usually do.");
		book("letters_never_sent", "Letters I Never Sent", "Dear Master BreadLoaf,\nI hope the End isn't too cold. Come visit sometime. Bread misses you, even though he won't say it.", "Dear FelixSyn,\nPlease stop landing on things.", "Dear Madison,\nThank you for always talking first, so I don't have to.\n\n- Lily");
	}

	public static Map<String, Book> all() {
		return Collections.unmodifiableMap(BOOKS);
	}

	/** The signed book, tagged with {lily_book: id} so advancements can tell which one it is. */
	public static ItemStack stack(String id) {
		Book book = BOOKS.get(id);
		if (book == null) {
			throw new IllegalArgumentException("No Lily book " + id);
		}
		List<Filterable<Component>> pages = new ArrayList<>();
		for (String page : book.pages()) {
			pages.add(Filterable.passThrough(Component.literal(page)));
		}
		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
		stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(book.title()), AUTHOR, 0, pages, true));
		CompoundTag tag = new CompoundTag();
		tag.putString("lily_book", id);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}
}
