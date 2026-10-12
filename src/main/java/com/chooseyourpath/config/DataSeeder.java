package com.chooseyourpath.config;

import com.chooseyourpath.entity.*;
import com.chooseyourpath.model.Role;
import com.chooseyourpath.model.StoryStatus;
import com.chooseyourpath.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.chooseyourpath.entity.*;


import java.time.LocalDateTime;
import java.util.List;

/**
 * Optionally loads sample stories when a real writer account already exists.
 * This seeder never creates demo reader, writer, or admin login accounts.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final StoryRepository stories;
    private final SceneRepository scenes;
    private final ChoiceRepository choices;
    private final StoryStyleRepository styles;

    public DataSeeder(UserRepository users, StoryRepository stories, SceneRepository scenes,
                      ChoiceRepository choices, StoryStyleRepository styles) {
        this.users = users;
        this.stories = stories;
        this.scenes = scenes;
        this.choices = choices;
        this.styles = styles;
    }

    private record SceneSpec(String t, String n, String d, String ending) {}
    private record ChoiceSpec(int from, String text, int to, String path, String cons) {}
    private record StorySpec(String title, String description, String genre, String theme,
                             String writingStyle, String narrative, String coverStyle,
                             List<String> styles, List<SceneSpec> scenes, List<ChoiceSpec> choices) {}

    @Override
    @Transactional
    public void run(String... args) {
        User writer = users.findByRole(Role.WRITER).stream().findFirst().orElse(null);
        if (writer == null) {
            // No writer account exists yet; do not create demo accounts or orphaned sample stories.
            return;
        }

        for (StorySpec spec : storySpecs()) {
            boolean exists = stories.findAll().stream().anyMatch(s -> s.getTitle().equals(spec.title()));
            if (exists) continue;
            seedStory(spec, writer);
        }
    }

    private void seedStory(StorySpec spec, User writer) {
        LocalDateTime now = LocalDateTime.now();
        Story story = new Story();
        story.setTitle(spec.title());
        story.setDescription(spec.description());
        story.setGenre(spec.genre());
        story.setTheme(spec.theme());
        story.setWritingStyle(spec.writingStyle());
        story.setNarrative(spec.narrative());
        story.setCoverStyle(spec.coverStyle());
        story.setStatus(StoryStatus.PUBLISHED);
        story.setCreator(writer);
        story.setReads(0);
        story.setCreatedAt(now);
        story.setUpdatedAt(now);
        story = stories.save(story);

        for (String style : spec.styles()) {
            StoryStyle ss = new StoryStyle();
            ss.setStory(story);
            ss.setStyleName(style);
            styles.save(ss);
        }

        List<SceneSpec> sceneSpecs = spec.scenes();
        Scene[] sceneIds = new Scene[sceneSpecs.size()];
        for (int i = 0; i < sceneSpecs.size(); i++) {
            SceneSpec sp = sceneSpecs.get(i);
            Scene sc = new Scene();
            sc.setStory(story);
            sc.setSceneOrder(i + 1);
            sc.setTitle(sp.t());
            sc.setNarration(sp.n());
            sc.setSceneDescription(sp.d());
            sc.setEnding(sp.ending() != null);
            sc.setEndingLabel(sp.ending());
            sceneIds[i] = scenes.save(sc);
        }

        for (ChoiceSpec cs : spec.choices()) {
            Choice c = new Choice();
            c.setScene(sceneIds[cs.from() - 1]);
            c.setNextScene(sceneIds[cs.to() - 1]);
            c.setChoiceText(cs.text());
            c.setPathName(cs.path());
            c.setConsequence(cs.cons());
            choices.save(c);
        }
    }

    private SceneSpec sc(String t, String n, String d) { return new SceneSpec(t, n, d, null); }
    private SceneSpec en(String t, String n, String d, String label) { return new SceneSpec(t, n, d, label); }
    private ChoiceSpec ch(int from, String text, int to, String path, String cons) { return new ChoiceSpec(from, text, to, path, cons); }

    private List<StorySpec> storySpecs() {
        List<SceneSpec> forestScenes = List.of(
            sc("The Edge of the Trees",
                "The last light of the village disappears behind Mira as she reaches the edge of the forest. Between the ancient trees, a single blue lantern flickers, impossibly far away and impossibly close at once. The voice that woke her — her own name, spoken softly — seems to come from everywhere now.",
                "A girl standing at the entrance of a vast glowing forest at night, a single blue lantern light flickering between enormous ancient trees, fireflies, mist"),
            sc("The Blue Lantern",
                "The lantern hangs from a crooked branch, blue as deep water, and it hums like a held breath. When Mira steps closer, the whisper returns: 'You came.' The light bends toward a narrower path leading off the main trail.",
                "A glowing blue lantern hanging from a crooked tree branch at night, humming light, a narrow path leading deeper into dark forest"),
            sc("The Fork of Whispers",
                "The path splits at a stone marker worn smooth by centuries of rain. To the left, the ground slopes down toward the sound of running water. To the right, the trees open around a fallen shrine gate. The lantern light trembles, as if it cannot decide which way to lean.",
                "A forest path splitting around an ancient moss-covered stone marker at night, one direction sloping toward a glowing river, the other toward a ruined shrine gate"),
            sc("Down to the River",
                "The hidden river runs silver in the dark, faster than any river has a right to run. Floating lanterns drift on its surface — dozens of them, each carrying a small paper boat. As Mira watches, one lantern bumps the bank and comes to rest at her feet. Inside the boat: a strip of bark with her name carved into it.",
                "A silver moonlit river at night carrying dozens of floating blue lanterns, paper boats, a girl kneeling at the mossy bank"),
            sc("The Ferryman's Question",
                "'The river remembers everyone who drinks from it,' says a voice. A lean fox sits on a flat stone, tail curled around its paws, eyes catching the lantern light. 'It sent boats for you. That is either a great honor, Mira — or a great debt.'",
                "A sleek fox with glowing eyes sitting on a flat stone beside a moonlit river at night, paper boats drifting past"),
            sc("The River's Memory",
                "Mira asks the river why it called her. The water stills, and in its surface she sees not her reflection but a smaller girl, years younger, running through these same trees with a paper lantern of her own. The river has known her longer than she has known herself. The fox watches her remember.",
                "A girl gazing into still moonlit water that reflects a memory of a smaller child carrying a paper lantern through a forest"),
            sc("The Fox's Bargain",
                "The fox's whiskers twitch. 'The voice you heard was the shrine's. It has been calling you since the night you first ran through these trees. I can carry you to it — my price is one true memory, given freely.' His eyes are ancient and kind and a little hungry, all at once.",
                "A mystical fox speaking with a girl at the edge of a glowing forest pool at night, floating embers, ancient kindly eyes"),
            sc("The Shrine Gate",
                "The fallen gate is older than the village, older than the road. Moss has taken the carved names that once ringed its arch, but beneath Mira's palm the stone is warm, like something sleeping. Beyond it, shrine lanterns bloom one by one down a corridor of trees.",
                "A fallen ancient shrine gate covered in moss at night, warm glowing stone, a corridor of blooming lantern lights beyond it"),
            sc("The Watchtower Path",
                "The watchtower rises black against the stars, its stairs collapsed halfway up. Someone has tied hundreds of small bells to the surviving railing, and though the night is windless, they are all ringing — softly, steadily, in unison, like an invitation.",
                "A tall ruined stone watchtower against a starry night sky, hundreds of small bells on its railing ringing in still air"),
            sc("The Bells Speak",
                "Each bell, Mira realizes, carries a scratch-mark: a name. Hundreds of names. The ringing is not wind — it is the tower counting. And it has just stopped counting, the way a speaker pauses before saying something important.",
                "Close view of small ancient bells engraved with names ringing on a stone railing at night, moonlight, hanging moss"),
            sc("The Keeper of the Tower",
                "At the top of the broken stairs sits a figure in a keeper's coat, lantern resting on one knee. 'Every keeper before me rang the bells to say: we are still here, the forest is still kind. I am very old now, and the bells need new hands.' The figure turns — and under the hood there is only gentle starlight.",
                "A hooded keeper figure with a lantern sitting at the top of ruined watchtower stairs at night, gentle starlight under the hood"),
            sc("The Name in the Shrine",
                "Deep in the shrine, Mira finds a wall of carved names — and one fresh hollow, exactly the shape of her own. The whispering was never a stranger. It was the forest, learning her name the way you learn a song: by singing it over and over until someone answers. Mira opens her mouth to answer.",
                "A girl before an ancient shrine wall of carved names, one glowing hollow shaped like a name, candlelight and drifting petals"),
            en("Answering the Forest",
                "Mira speaks her own name aloud — not as a question, but as an answer. The lanterns flare blue, the river applauds in small silver waves, and the bells in the distant tower ring once, joyfully. The forest exhales. She is not lost. She is found.",
                "A girl speaking aloud in a forest erupting with blue lantern light, silver river waves, distant joyful bells, magical night",
                "THE VOICE ANSWERED"),
            en("The Memory Given",
                "Mira gives the fox her smallest memory — the smell of rain on the village well. The fox closes its eyes, then leads her along secret paths no map has ever held, to the shrine, to the hollow with her name. 'The forest keeps what it loves,' he says. 'Now it keeps you gently.'",
                "A fox leading a girl through a secret forest tunnel of intertwined trees glowing with blue lantern light at night",
                "THE FOX'S COMPANION"),
            en("The New Keeper",
                "Mira climbs the broken stairs, and they hold. She takes the lantern, and it is warm. The bells fall silent under her hands — then ring out, loud and bright, a new rhythm that is entirely hers. Somewhere below, the fox smiles. The forest has a keeper again.",
                "A girl standing atop a ruined watchtower ringing hundreds of bells at night, holding a bright blue lantern, stars wheeling above",
                "THE NEW KEEPER"),
            en("The River's Daughter",
                "Mira steps into the silver river, and it does not feel like water — it feels like memory, holding her up. The lantern boats gather around her, and she understands at last: the river called its daughter home. Every boat was a year of waiting. Every lantern was her name, spoken softly.",
                "A girl standing on moonlit water surrounded by dozens of floating blue lanterns and paper boats, arms open, magical night",
                "THE RIVER'S DAUGHTER"));

        List<ChoiceSpec> forestChoices = List.of(
            ch(1, "Follow the blue lantern", 2, "Path of the Lantern", "Mira finds the humming lantern and the narrow path it guards."),
            ch(1, "Call out into the dark", 3, "Path of the River", "Her voice startles the forest; the whisper leads her toward water."),
            ch(2, "Take the lantern from its branch", 8, "Path of the Watchtower", "The lantern goes dark in her hands and points toward the tower."),
            ch(2, "Bow to the lantern and ask permission", 3, "Path of the Shrine", "The light steadies, approving; the way to the shrine opens."),
            ch(3, "Follow the sound of running water", 4, "Path of the River", "Mira descends to the hidden river and its drifting boats."),
            ch(3, "Step through the fallen shrine gate", 8, "Path of the Shrine", "The moss-covered gate is warm beneath her palm."),
            ch(4, "Pick up the paper boat with her name", 5, "Path of the River", "The carved bark inside confirms the river knows her."),
            ch(4, "Wade into the silver water", 16, "Path of the River", "The river rises gently to meet her — the water remembers."),
            ch(5, "Ask the fox about the voice", 6, "Path of the Fox", "The fox speaks plainly: the shrine has called her since childhood."),
            ch(5, "Ask the fox the way to the shrine", 7, "Path of the Fox", "The fox offers to guide her — for the price of one true memory."),
            ch(6, "Share a childhood memory with the river", 13, "Path of the River", "The memory offered, the river shows her the way home to itself."),
            ch(6, "Ask the fox to guide her onward", 7, "Path of the Fox", "The fox agrees, naming his price."),
            ch(7, "Give the fox one true memory", 14, "Path of the Fox", "The fox leads her by secret paths to the shrine of her name."),
            ch(7, "Decline and seek the shrine alone", 8, "Path of the Shrine", "The fox bows and vanishes; the gate is warm when she finds it."),
            ch(8, "Speak her name at the carved hollow", 13, "Path of the Shrine", "The forest has been learning her name; she answers at last."),
            ch(8, "Follow the corridor of lanterns deeper", 9, "Path of the Watchtower", "The lantern corridor ends at the ringing tower."),
            ch(9, "Climb the broken stairs", 10, "Path of the Watchtower", "The stairs hold; the bells ring louder as she ascends."),
            ch(9, "Listen to what the bells are saying", 10, "Path of the Watchtower", "The pattern is a count — a pause before words."),
            ch(10, "Answer the bells with her own name", 11, "Path of the Watchtower", "The ringing stops. The keeper expects her."),
            ch(10, "Cut one bell free to keep", 11, "Path of the Watchtower", "The tower permits it; the rest ring approval."),
            ch(11, "Accept the keeper's lantern", 15, "Path of the Watchtower", "Mira becomes the forest's new keeper."),
            ch(11, "Ask the keeper about the voice first", 12, "Path of the Shrine", "'The voice is the shrine,' says the keeper. 'It is waiting for you.'"),
            ch(12, "Answer the forest aloud", 13, "Path of the Shrine", "She speaks her name; the forest answers in blue light."),
            ch(12, "Return to the keeper with the answer", 15, "Path of the Watchtower", "Her answer completes the keeper's long vigil."));

        List<SceneSpec> clockScenes = List.of(
            sc("Eleven O'Clock, Forever",
                "The whole town is holding its breath. Every clock in Elsie's workshop shows eleven — and has for an hour that refuses to end. From the square she looks up at the tower: the great hands are frozen, and behind the clock face, something is ticking out of rhythm.",
                "A young apprentice girl on a cobblestone square at night looking up at a huge clock tower frozen at eleven, gas lamps, moths circling lamps"),
            sc("The Gear Room",
                "Inside, the gear room smells of oil and thunder. The great gears are stopped — all but one small brass wheel at the center, turning against every rule of clockwork, ticking out a pattern rather than a time. Elsie has read every manual her master owned. This pattern is not in any of them.",
                "A vast gear room of enormous stopped brass gears, one small wheel turning, lamplight and dust, a girl climbing among them"),
            sc("The Crossroads of Gears",
                "The stopped mechanism splits the room in two. Upward, a spiral stair hugs the inner wall, rising toward the bell chamber. Along the back, a low door Elsie has never seen sits behind the pendulum — half hidden, its keyhole warm. The ticking comes from both directions now.",
                "A dark clocktower interior where a spiral stair rises and a low half-hidden door waits behind a giant pendulum, brass and shadow"),
            sc("The Clockmaker's Letter",
                "The bell chamber is quiet as held breath. On the great bell's rim, weighted by a wrench, sits a letter in her master's unmistakable hand: 'If the clock has stopped at eleven, then I have failed, and you must choose what midnight means. I am sorry to leave this to you, Elsie.'",
                "A handwritten letter on aged paper resting on the rim of an enormous bronze bell in a dark tower chamber, moonlight through louvers"),
            sc("The Locked Room",
                "The low door opens on a room the tower has kept secret for forty years: a clockmaker's workshop, untouched, everything in its place under a skin of dust. On the bench, a broken pocket watch lies with its case open — and inside, instead of a portrait, a tiny painted door.",
                "A secret dusty clockmaker workshop inside a tower, workbench with an open broken pocket watch whose face shows a tiny painted door, candlelight"),
            sc("The Watchmaker's Bench",
                "The tools on the bench are arranged for a left-handed craftsman. Her master is right-handed. The dust says forty years; the oil on the whetstone says this morning. Someone has been keeping this room alive, patiently, all along.",
                "A watchmaker workbench with left-handed tools, oil-slick whetstone, dust and lamplight, tiny gears arranged in rows"),
            sc("The Passage Behind the Weights",
                "Behind the driving weights, the stone is false — a door with no handle that swings at a touch. Beyond, stairs corkscrew down inside the tower's leg, toward the old tunnels that once linked the town's four clocks. The air that rises is warm and smells of metal and rain.",
                "A hidden stone passage door behind enormous clock weights, corkscrew stairs descending into warm darkness, faint lamplight below"),
            sc("The Heart of the Clock",
                "At the tower's heart, behind the clock face, hangs the escapement — the soul of the machine. And wedged between its teeth, deliberate and impossible, is a single brass key. The clock has not broken. It has been stopped on purpose. Stopped, and locked.",
                "The enormous escapement mechanism behind a giant clock face, a single brass key wedged between its teeth, dramatic lamplight"),
            sc("The Other Clockmaker",
                "The passage opens into the tunnels, and the tunnels into a second, smaller workshop — a mirror of the first. At its bench sits an old man with his left hand bandaged, winding a watch that ticks in the same pattern as the brass wheel above. 'You found the letter,' he says. 'Good. Then you know midnight needs choosing.'",
                "An old clockmaker with a bandaged hand at a bench in an underground workshop lit by oil lamps, gears and keys on every wall"),
            sc("The Four Clocks",
                "A map on the wall shows the town's four clocks, linked beneath the streets like four moons around one planet. Three dials are drawn at eleven. The fourth is blank. 'Every clock needs a heart to keep,' says the old man. 'Mine is nearly wound out. The tower needs one that will not stop.'",
                "An old map of four linked clocks, three dials drawn at eleven and one blank, pinned above a workbench in lamplight"),
            sc("The Winding Chair",
                "In the center of the tunnel workshop stands a chair of brass and leather, wired into the tower's mainspring with silver thread. Whoever sits in it becomes the winding — the heart that keeps the hour. The old man looks at it the way one looks at a beloved instrument about to be played for the last time.",
                "A strange brass winding chair wired with silver thread into a giant mainspring in an underground workshop, warm lamplight and shadow"),
            en("Midnight, Chosen",
                "Elsie sets the key in the escapement and turns. Above her the town hears one deep, clean stroke of the bell — and then the normal, patient tick of a clock that has decided to keep going. Time moves again. In the tower room, eleven becomes midnight at last, and midnight becomes tomorrow.",
                "A girl turning a brass key inside a giant clock escapement as golden light floods the gear room, gears beginning to move",
                "MIDNIGHT, CHOSEN"),
            en("The Successor's Hands",
                "Elsie winds the watch that was her master's, then takes the left-handed tools from the secret bench and slides them into her apron. She climbs the spiral stair as the bell strikes twelve. The town wakes to a clock that runs, and a tower that has a new keeper of its hours.",
                "A girl climbing a spiral tower stair carrying small tools as the great bell strikes overhead, dawn light through the louvers",
                "THE SUCCESSOR'S HANDS"),
            en("The Two Clockmakers",
                "Elsie pulls the bandage from the old man's hand — the scar of a master winder, earned in a tower like this one, long ago. 'Then we do it together,' she says. He laughs, and the sound is the best oiled click she has ever heard. Two clockmakers climb toward the escapement, and midnight waits politely.",
                "A young woman and an old clockmaker climbing tower stairs side by side with lanterns, gears turning around them, hopeful light",
                "THE TWO CLOCKMAKERS"),
            en("The Hour That Was Kept",
                "Elsie sits in the winding chair and takes the silver thread in both hands. The mainspring hums, the tower shivers, and every clock in the town — all four, deep beneath the streets — strikes midnight together, a chord like a held breath released. Somewhere above, the town celebrates an hour that never noticed it was saved.",
                "A girl in a brass winding chair holding silver threads as light flows up through giant gears into a clock tower at midnight",
                "THE HOUR THAT WAS KEPT"),
            en("The Door in the Watch",
                "The tiny painted door in the pocket watch opens when Elsie turns the crown. Inside is a staircase the size of her finger — and it is not a painting at all. The watch ticks. From somewhere very far below, very small, a bell rings midnight. Elsie smiles, and climbs down into the watch.",
                "A tiny glowing door open in the face of a pocket watch revealing a miniature staircase, magical blue light, a girl peering in wonder",
                "THE DOOR IN THE WATCH"));

        List<ChoiceSpec> clockChoices = List.of(
            ch(1, "Climb the tower's outer stairs", 2, "The Mechanism Path", "Elsie enters the gear room and finds the impossible wheel."),
            ch(1, "Search the square for other stopped clocks", 3, "The Passage Path", "Every clock is frozen at eleven; the tower hides a door."),
            ch(2, "Climb toward the bell chamber", 4, "The Letter Path", "A letter from her master waits on the bell's rim."),
            ch(2, "Investigate the door behind the pendulum", 5, "The Locked Room Path", "A secret workshop — and a pocket watch with a painted door."),
            ch(3, "Enter the tower by the hidden door", 5, "The Locked Room Path", "The keyhole is warm; the room beyond is untouched."),
            ch(3, "Follow the ticking into the undercroft", 7, "The Passage Path", "A false stone door swings open behind the weights."),
            ch(4, "Read the letter aloud in the chamber", 8, "The Letter Path", "The words unlock the tower's heart: a key in the escapement."),
            ch(4, "Search the bell chamber for more of his hand", 6, "The Locked Room Path", "A brass fitting points down — toward the locked room."),
            ch(5, "Study the broken pocket watch", 6, "The Locked Room Path", "The bench says the room has been kept alive for forty years."),
            ch(5, "Wind the pocket watch", 16, "The Watch Path", "The painted door opens onto a very small, very real staircase."),
            ch(6, "Follow the fresh oil to the false stone", 7, "The Passage Path", "Someone has been coming and going this very morning."),
            ch(6, "Take the left-handed tools with her", 8, "The Letter Path", "The tools fit her hand perfectly. The heart of the clock waits."),
            ch(7, "Descend into the old tunnels", 9, "The Passage Path", "The tunnels lead to a mirror workshop and its keeper."),
            ch(7, "Rally and force the false door to the gears", 8, "The Mechanism Path", "A shortcut to the heart of the stopped clock."),
            ch(8, "Turn the brass key at once", 12, "The Mechanism Path", "The clock chooses to keep going — midnight, at last."),
            ch(8, "Search for whoever locked the clock", 9, "The Passage Path", "The key fits the tunnels as well as the tower."),
            ch(8, "Climb back and take up the master's tools", 13, "The Letter Path", "The tower will need hands again tomorrow."),
            ch(9, "Ask what midnight needs choosing", 10, "The Winding Path", "Four clocks, three hearts wound out, one heart needed."),
            ch(9, "Ask who he is", 13, "The Two Clockmakers Path", "The other clockmaker — the first apprentice of the tower."),
            ch(9, "Join the old clockmaker as his partner", 14, "The Two Clockmakers Path", "Two hands are better than one — and two clocks, better than four."),
            ch(10, "Study the map of the four clocks", 11, "The Winding Path", "The blank dial is the tower. The winding chair is the answer."),
            ch(10, "Offer to find a heart that will not stop", 11, "The Winding Path", "The old man leads her to the winding chair."),
            ch(11, "Sit in the winding chair", 15, "The Winding Path", "Elsie becomes the hour that was kept."),
            ch(11, "Refuse the chair and climb to the escapement", 12, "The Mechanism Path", "Some midnights are chosen with a key, not a chair."));

        List<SceneSpec> starScenes = List.of(
            sc("The Fading",
                "Every child of Avalon knows the sky has only one true star left. Tonight, for the first time in three hundred years, it flickers. From the observatory hill, Mira of the Avalon Survey watches it dim — and makes a promise to it she does not yet know how to keep.",
                "A young explorer on an observatory hill at night watching a single flickering star over a fantasy kingdom, telescope silhouette, deep blue sky"),
            sc("Moon Valley",
                "Moon Valley earns its name only at night, when the pale flowers open and turn their faces upward like a crowd of small moons. The star's light falls across the valley in a silver road — and part of the road is moving. Something is walking the light toward her.",
                "A valley of glowing white moonflowers at night, a silver road of starlight across the flowers, a small distant figure walking the light"),
            sc("The Valley Fork",
                "The silver road forks around a basin of black glass where the valley stores its rain of falling stars. To the left, the road climbs toward the Crystal Bridge, humming faintly. To the right, it descends toward the Forgotten Temple, where the moonflowers grow thick as snowdrifts.",
                "A silver starlit road forking around a basin of black glass, one branch rising toward a crystal bridge, the other descending to a buried temple"),
            sc("The Crystal Bridge",
                "The Crystal Bridge spans the sky-ridge like a frozen waterfall, and it is singing. Each step raises a different note. Halfway across, Mira finds the note that does not belong — a dead crystal, dark as coal, and on its surface, clawed into it, one word: 'LISTEN.'",
                "A transparent crystal bridge across a mountain ridge at night under stars, one dark dead crystal among the glowing ones, wind-blurred hair"),
            sc("The Song of the Bridge",
                "Mira listens the way the dead crystal asked. The bridge is not singing randomly — it is counting. The way a lighthouse counts, the way a heart counts. And it is counting down. The star's light overhead stutters, and the bridge's song drops an octave, as if grieving.",
                "A girl kneeling on a crystal bridge at night listening to glowing crystals, starlight overhead flickering, petals of light drifting"),
            sc("The Bridge Keeper",
                "'It sings what the star tells it,' says a voice like struck glass. The bridge keeper is made of the same crystal as her bridge, transparent in the starlight, sorrowful and bright. 'It has sung for three hundred years. Soon it will sing an ending.' She smiles, and it is like frost forming. 'Unless someone changes the song.'",
                "A translucent crystalline woman standing on a glowing crystal bridge at night, sorrowful and luminous, stars behind"),
            sc("The Forgotten Temple",
                "The temple was buried on purpose. Moonflowers crowd its collapsed roof, and inside, the dark is total — until Mira's lantern finds the wall of star-maps. Whoever made them mapped the sky of Avalon for a thousand years, and on the newest map, one star is drawn with a question mark beside it.",
                "A buried ancient temple interior covered in glowing star maps and moonflower vines, a lantern-lit explorer touching the newest map"),
            sc("The Temple's Question",
                "The newest map is inked in a careful, living hand: the star, drawn fainter each year, with the question mark renewed annually like a prayer. Beneath it, a line of script: 'We asked the sky for a keeper. The sky asked who would keep the keeper.'",
                "A glowing star map with one faint star marked by a question mark, ancient script beneath, lantern light, drifting moonflower petals"),
            sc("The Sky Ruins",
                "No road reaches the Sky Ruins; they float above the kingdom like the bones of a thought. Mira climbs the last of the moonflower stalks — grown towering and strong, fed by three hundred years of starlight — and steps off into thin air onto marble that remembers being a palace.",
                "Floating marble ruins above a moonlit kingdom at night, giant moonflower stalks reaching up to them, a tiny figure stepping onto pale stone"),
            sc("The Star Garden",
                "At the heart of the ruins is a garden, and in the garden grows the last star — a tree of white fire, its roots in a well of night, its fruit small and impossibly bright. Kneeling at its trunk is the guardian, tending it with cupped hands, and the guardian is weeping.",
                "A tree of white fire growing in a marble garden in the sky at night, its fruit like small stars, a kneeling weeping guardian of starlight"),
            sc("The Guardian's Confession",
                "'Stars are not born,' the guardian says. 'They are kept. I have kept this one alone since the kingdom forgot the tending songs.' The guardian's hands are cracked, leaking light like hourglass sand. 'I am almost empty, explorer. Tell me what Avalon remembers.'",
                "A guardian of starlight with cracked glowing hands kneeling before a tree of white fire in a sky garden at night"),
            en("The Star Restored",
                "Mira answers the only way she can: with the tending songs, the ones every Avalon child learns without knowing what they are for. The star-tree leans into the sound. Light climbs the guardian's cracked hands and knits them whole. The star, above and below, steadies.",
                "A girl singing a tending song before a tree of white fire as light knits a guardian's cracked hands whole, stars steadying overhead",
                "THE STAR RESTORED"),
            en("The New Song",
                "Mira does not only sing the old songs — she adds her own verse, the one the bridge taught her, the one the valley hums. The star-tree takes the new song the way soil takes rain. When dawn comes, the star is still there, and it is not alone: two new sparks burn beside it.",
                "A girl singing a new song before a star-tree as two new sparks kindle beside it in a dawn-lit sky garden",
                "THE NEW SONG"),
            en("The Second Keeper",
                "'Then keep it with me,' Mira says, and takes the guardian's cracked hands in hers. Light flows between them like a handshake across three hundred years. The well of night accepts them both, and the garden's tree burns brighter than it has since the first map was drawn.",
                "Two figures holding glowing hands beside a blazing star-tree in a sky garden, light flowing between them, dawn approaching",
                "THE SECOND KEEPER"),
            sc("The Fruit of the Star",
                "Mira picks one fruit of white fire, and it is warm as a heart in her palm. The guardian does not stop her. 'It will grow wherever you plant it,' the guardian says. 'Stars want to be many things. Mostly, they want to be needed.'",
                "A girl holding a fruit of white fire in her palm before a star-tree, a luminous guardian watching with hope, sky garden at night"),
            en("The Garden Below",
                "Mira carries the fruit down the moonflower stalks, through the valley of upturned flowers, and plants it on the observatory hill where every child of Avalon can see it grow. In time it becomes a small new star-tree, and the children learn the tending songs again — this time knowing why.",
                "A girl planting a glowing star fruit on an observatory hill as children gather around in a moonlit kingdom, a new small star-tree sprouting",
                "THE GARDEN BELOW"));

        List<ChoiceSpec> starChoices = List.of(
            ch(1, "Follow the silver road into Moon Valley", 2, "The Valley Path", "The moonflowers open a road of starlight."),
            ch(1, "Study the star from the observatory first", 2, "The Valley Path", "The telescope confirms it: the star is counting down."),
            ch(2, "Follow the figure walking the light", 3, "The Bridge Path", "The road climbs toward the singing Crystal Bridge."),
            ch(2, "Descend toward the buried temple", 7, "The Temple Path", "The moonflowers part to reveal the Forgotten Temple."),
            ch(3, "Cross the Crystal Bridge", 4, "The Bridge Path", "Each step rings; one crystal is dark, clawed with 'LISTEN.'"),
            ch(3, "Circle the black-glass basin", 7, "The Temple Path", "Fallen-star glass points the way to the buried temple."),
            ch(4, "Listen as the dead crystal asked", 5, "The Bridge Path", "The bridge is counting down, and grieving."),
            ch(4, "Press on toward the Sky Ruins", 9, "The Sky Path", "The bridge ends at the foot of the floating ruins."),
            ch(5, "Call out to whoever tends the song", 6, "The Bridge Path", "The bridge keeper answers, sorrowful and bright."),
            ch(5, "Record the song in her survey book", 9, "The Sky Path", "The count will matter later; the ruins are next."),
            ch(6, "Ask how to change the song", 10, "The Sky Path", "'Ask the temple,' says the keeper. 'Then ask the sky.'"),
            ch(6, "Offer to help tend the bridge", 9, "The Sky Path", "The keeper teaches her the bridge's true name as a gift."),
            ch(7, "Read the wall of star-maps", 8, "The Temple Path", "A thousand years of maps, one question renewed like a prayer."),
            ch(7, "Light the temple's dead lanterns", 8, "The Temple Path", "The lanterns reveal the temple's question in full."),
            ch(8, "Answer the temple's question aloud", 10, "The Sky Path", "'I will keep the keeper,' she says — and the ruins hear."),
            ch(8, "Copy the oldest map for the journey", 9, "The Sky Path", "The first map shows the way up the moonflower stalks."),
            ch(9, "Enter the star garden", 10, "The Sky Path", "A tree of white fire — and a guardian weeping at its roots."),
            ch(9, "Climb the highest ruined tower first", 10, "The Sky Path", "From above, the garden is unmistakable: the heart of the sky."),
            ch(10, "Share the tending songs of Avalon", 12, "The Valley Path", "The star leans into songs it has not heard in centuries."),
            ch(10, "Ask the guardian what the star needs", 11, "The Sky Path", "The confession: stars are kept, and the keeper is empty."),
            ch(10, "Add a new verse to the old songs", 13, "The New Song Path", "The star accepts the new song — and kindles two sparks more."),
            ch(11, "Take the guardian's hands and keep it together", 14, "The Sky Path", "Two keepers, one light, three hundred years answered."),
            ch(11, "Pick a fruit of the star-tree", 15, "The Garden Path", "The guardian permits it; stars want to be needed."),
            ch(11, "Promise to teach the songs to the kingdom", 13, "The New Song Path", "A promise the whole sky seems to hear."),
            ch(15, "Plant the fruit where all can see", 16, "The Garden Path", "A garden begins below, and the children relearn the songs."));

        return List.of(
            new StorySpec(
                "The Whispering Forest",
                "A voice in the night calls Mira by name. Beyond the village fence, the ancient forest is waiting — and so is whatever has been whispering for her.",
                "Fantasy / Mystery", "Curiosity vs. Caution", "Dark but hopeful",
                "Mira, a young traveler, hears a mysterious voice calling her name from the ancient forest. Drawn past the safety of the village, she follows traces of a blue lantern light deeper into the trees, where a forgotten shrine, a talking fox, a hidden river and an abandoned watchtower each guard their own truth about the voice — and about Mira herself.",
                "Watercolor", List.of("Classic Storybook", "Watercolor", "Dark Forest", "Dreamlike"),
                forestScenes, forestChoices),
            new StorySpec(
                "The Clockmaker's Last Hour",
                "The tower clock has stopped — one hour before midnight, and time itself seems to be holding its breath. Apprentice clockmaker Elsie climbs into the gears to find out why.",
                "Mystery / Fantasy", "Duty vs. Truth", "Precise, atmospheric, gently uncanny",
                "Elsie, a young apprentice clockmaker, discovers the town's clock tower frozen at eleven. Inside she finds a hidden mechanism, a mysterious letter, a locked room, a broken pocket watch and a secret passage. Her choices carry her through the gears toward the clockmaker's final secret — and several different midnights.",
                "Vintage Illustration", List.of("Vintage Illustration", "Ink", "Gothic", "Paper Cut"),
                clockScenes, clockChoices),
            new StorySpec(
                "The Last Star of Avalon",
                "The final star that guards the kingdom of Avalon is dying. A young explorer sets out across Moon Valley and the Crystal Bridge to learn why — and what it will cost to keep the sky alight.",
                "Adventure / Fantasy", "Hope vs. Sacrifice", "Sweeping, luminous, sincere",
                "When the last star of Avalon begins to fade, a young explorer journeys through Moon Valley, across the Crystal Bridge, into the Forgotten Temple and up to the Sky Ruins to discover who tends the stars — and what the sky asks in return. Every path reveals a different truth about the kingdom's light.",
                "Celestial", List.of("Fairy Tale", "Comic Illustration", "Mystical", "Celestial"),
                starScenes, starChoices));
    }
}
