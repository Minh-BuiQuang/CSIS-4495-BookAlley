namespace BookAlleyWebApi.RestModels
{
    public class PostResponse
    {
        public long Id { get; set; }
        public required string PosterName { get; set; }
        public required long PosterId { get; set; }

        public string? Author { get; set; }
        public string? ISBN { get; set; }
        public required string BookTitle { get; set; }
        public string? Image { get; set; }
        public required string Location { get; set; }
        public string? Note { get; set; }
        public required DateTimeOffset DatePosted { get; set; }

    }
}
